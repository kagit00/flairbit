package com.dating.flairbit.service.match.suggestions;


import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.processor.MatchSuggestionsStorageProcessor;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MatchSuggestionsStorageServiceImpl implements MatchSuggestionsStorageService {
    private final MatchSuggestionsStorageProcessor processor;
    private volatile boolean shutdownInitiated = false;

    public MatchSuggestionsStorageServiceImpl(MatchSuggestionsStorageProcessor processor) {
        this.processor = processor;
    }

    @PreDestroy
    private void shutdown() {
        shutdownInitiated = true;
    }

    @Override
    public CompletableFuture<Void> saveMatchSuggestions(Flux<MatchSuggestion> matchSuggestions, String groupId) {
        if (shutdownInitiated) {
            log.warn("Save aborted due to shutdown");
            return CompletableFuture.failedFuture(new IllegalStateException("MatchSuggestionsStorageService is shutting down"));
        }

        log.info("Saving match suggestions for groupId={}", groupId);
        return processor.saveMatchSuggestions(matchSuggestions, groupId)
                .orTimeout(1_800_000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    log.error("Failed to save match suggestions for groupId={}: {}", groupId, throwable.getMessage());
                    throw new CompletionException("Save failed", throwable);
                });
    }

    @Override
    public CompletableFuture<List<MatchSuggestion>> retrieveMatchSuggestions(String participantUsername, String groupId) {
        if (shutdownInitiated) {
            log.warn("Retrieve aborted for participantUsername={} and groupId={} due to shutdown", participantUsername, groupId);
            return CompletableFuture.failedFuture(new IllegalStateException("MatchSuggestionsStorageService is shutting down"));
        }

        return processor.findFilteredSuggestions(participantUsername, groupId)
                .orTimeout(1_800_000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    log.error("Failed to retrieve match suggestions for participantUsername={}, groupId={}: {}", participantUsername, groupId, throwable.getMessage());
                    throw new CompletionException("Retrieve failed", throwable);
                });
    }
}