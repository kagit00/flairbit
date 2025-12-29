package com.dating.flairbit.processor;

import com.dating.flairbit.config.factory.BinaryCopyInputStream;
import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.utils.db.QueryUtils;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


import reactor.core.scheduler.Schedulers;
import java.sql.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSuggestionsStorageProcessor {

    private static final long OPERATION_TIMEOUT_MS = 1_800_000; // 30 mins

    private final HikariDataSource dataSource;
    private final MeterRegistry meterRegistry;
    private final ExecutorService ioExecutor;
    private volatile boolean shutdownInitiated = false;

    @Value("${match-suggestions.import.batch-size:500}")
    private int batchSize;

    @Value("${domain-id}")
    private String domainId;

    @PreDestroy
    private void shutdown() {
        shutdownInitiated = true;
        try {
            ioExecutor.shutdown();
            if (!ioExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("I/O executor did not terminate gracefully in 60 seconds. Forcing shutdown.");
                ioExecutor.shutdownNow();
            }
            dataSource.close();
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    public CompletableFuture<Void> saveMatchSuggestions(Flux<MatchSuggestion> matches, String groupId) {
        if (shutdownInitiated) {
            log.warn("Save aborted for groupId={} due to shutdown", groupId);
            return CompletableFuture.failedFuture(new IllegalStateException("MatchSuggestionsStorageProcessor is shutting down"));
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        AtomicLong totalProcessed = new AtomicLong();

        return matches
                // ✅ BOUNDED BATCHING: Prevents unbounded queue growth
                .bufferTimeout(batchSize, Duration.ofSeconds(1))
                .concatMap(batch -> {
                    log.info("Queueing save of {} MatchSuggestions for groupId={}, domainId={}",
                            batch.size(), groupId, domainId);
                    totalProcessed.addAndGet(batch.size());

                    // ✅ Run DB write on bounded thread-pool WITHOUT async pile-up
                    return Mono.fromRunnable(() -> saveBatch(batch, groupId))
                            .subscribeOn(Schedulers.fromExecutor(ioExecutor))
                            .onErrorResume(e -> {
                                meterRegistry.counter("match_suggestions_storage_errors_total",
                                                "groupId", groupId,
                                                "error_type", e.getClass().getSimpleName())
                                        .increment(batch.size());
                                log.error("Failed to save {} match_suggestions for groupId={}: {}",
                                        batch.size(), groupId, e.getMessage(), e);
                                return Mono.empty();
                            });
                })
                .limitRate(2) // ✅ Controls request rate to downstream
                .then()
                .doOnTerminate(() -> {
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(
                            sample.stop(meterRegistry.timer("match_suggestions_storage_duration", "groupId", groupId))
                    );
                    meterRegistry.counter("match_suggestions_storage_matches_saved_total", "groupId", groupId)
                            .increment(totalProcessed.get());
                    log.info("Saved {} match_suggestions for groupId={} in {} ms",
                            totalProcessed.get(), groupId, durationMs);
                })
                .timeout(Duration.ofMillis(OPERATION_TIMEOUT_MS))
                .toFuture()
                .exceptionally(throwable -> {
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(
                            sample.stop(meterRegistry.timer("match_suggestions_storage_duration", "groupId", groupId))
                    );
                    meterRegistry.counter("match_suggestions_storage_errors_total", "groupId", groupId,
                                    "error_type", throwable.getClass().getSimpleName())
                            .increment(totalProcessed.get());
                    log.error("Failed to save match_suggestions for groupId={}: {}",
                            groupId, throwable.getMessage(), throwable);
                    return null; // CompletableFuture<Void>
                });
    }

    @Transactional
    @Retryable(value = {SQLException.class, TimeoutException.class},
            backoff = @org.springframework.retry.annotation.Backoff(delay = 1000, multiplier = 2))
    private void saveBatch(List<MatchSuggestion> batch, String groupId) {
        if (shutdownInitiated) {
            log.warn("Batch save aborted for groupId={} due to shutdown", groupId);
            throw new IllegalStateException("MatchSuggestionsStorageProcessor is shutting down");
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        log.debug("Saving batch of {} MatchSuggestions for groupId={}", batch.size(), groupId);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            conn.createStatement().execute("SET synchronous_commit = OFF");
            conn.createStatement().execute(QueryUtils.getMatchSuggestionsTempTableSQL());

            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));
            InputStream binaryStream = new BinaryCopyInputStream<>(batch, new MatchSuggestionSerializer(batch, groupId));

            copyManager.copyIn(
                    "COPY temp_match_suggestions (id, group_id, participant_id, matched_participant_id, compatibility_score, match_suggestion_type, created_at) " +
                            "FROM STDIN WITH (FORMAT BINARY)",
                    binaryStream);

            try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.getUpsertMatchSuggestionsSql())) {
                stmt.executeUpdate();
            }

            conn.commit();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    sample.stop(meterRegistry.timer("match_suggestions_storage_batch_duration", "groupId", groupId))
            );
            meterRegistry.counter("match_suggestions_storage_batch_processed_total", "groupId", groupId)
                    .increment(batch.size());
            log.debug("Saved batch of {} match_suggestions for groupId={} in {} ms",
                    batch.size(), groupId, durationMs);
        } catch (SQLException | IOException e) {
            log.error("Batch save failed for groupId={}: {}", groupId, e.getMessage(), e);
            throw new CompletionException("Batch save failed", e);
        }
    }

    public CompletableFuture<List<MatchSuggestion>> findFilteredSuggestions(String participantUsername, String groupId) {
        if (shutdownInitiated) {
            log.warn("Retrieve aborted for participantUsername={} and groupId={} due to shutdown",
                    participantUsername, groupId);
            return CompletableFuture.failedFuture(new IllegalStateException("MatchSuggestionsStorageProcessor is shutting down"));
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("Retrieving match suggestions for participantUsername={}, groupId={}",
                participantUsername, groupId);

        return CompletableFuture.supplyAsync(() -> {
            List<MatchSuggestion> suggestions = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT id, group_id, participant_id, matched_participant_id, compatibility_score, created_at, match_suggestion_type " +
                                 "FROM match_suggestions WHERE participant_id = ? AND group_id = ?")) {

                stmt.setString(1, participantUsername);
                stmt.setString(2, groupId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    MatchSuggestion suggestion = new MatchSuggestion();
                    suggestion.setId(rs.getObject("id", UUID.class));
                    suggestion.setGroupId(rs.getString("group_id"));
                    suggestion.setParticipantId(rs.getString("participant_id"));
                    suggestion.setMatchedParticipantId(rs.getString("matched_participant_id"));
                    suggestion.setCompatibilityScore(rs.getDouble("compatibility_score"));
                    suggestion.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    suggestion.setMatchSuggestionType(rs.getString("match_suggestion_type"));
                    suggestions.add(suggestion);
                }

                return suggestions;
            } catch (SQLException e) {
                throw new CompletionException("Failed to retrieve match suggestions", e);
            }
        }, ioExecutor).whenComplete((result, throwable) -> {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    sample.stop(meterRegistry.timer("match_suggestions_retrieve_duration", "groupId", groupId))
            );

            if (throwable != null) {
                meterRegistry.counter("match_suggestions_retrieve_errors_total", "groupId", groupId,
                                "error_type", throwable.getClass().getSimpleName())
                        .increment();
                log.error("Failed to retrieve match suggestions for participantUsername={}, groupId={}: {}",
                        participantUsername, groupId, throwable.getMessage(), throwable);
            } else {
                meterRegistry.counter("match_suggestions_retrieve_total", "groupId", groupId)
                        .increment(result.size());
                log.info("Retrieved {} match suggestions for participantUsername={}, groupId={} in {} ms",
                        result.size(), participantUsername, groupId, durationMs);
            }
        }).orTimeout(OPERATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
}