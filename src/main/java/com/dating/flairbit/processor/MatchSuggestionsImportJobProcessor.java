package com.dating.flairbit.processor;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.MatchSuggestionDTO;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.config.factory.ResponseFactory;
import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.repo.MatchSuggestionsImportJobRepository;
import com.dating.flairbit.service.match.suggestions.MatchSuggestionsStorageService;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.media.praquet.ParquetParser;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSuggestionsImportJobProcessor {
    private static final String TOPIC = "flairbit-match-suggestions-transfer-job-status-retrieval";
    private static final int MAX_BATCH_SIZE = 10000;

    private final MatchSuggestionsImportJobRepository repository;
    private final MatchSuggestionsStorageService service;
    private final ResponseFactory<MatchSuggestionDTO> factory;
    private final FlairBitProducer producer;
    private final ExecutorService matchSuggestionsImportExecutor;
    private final TransactionTemplate transactionTemplate;

    @Value("${domain-id}")
    private String domainId;

    public CompletableFuture<Object> process(UUID jobId,
                                             MultipartFile file,
                                             String groupId,
                                             int batchSize) throws IOException {
        log.info("Job {} started: groupId={}, file={}, size={} bytes",
                jobId, groupId, file.getOriginalFilename(), file.getSize());

        transactionTemplate.execute(status -> {
            repository.updateStatus(jobId, JobStatus.PROCESSING);
            return null;
        });

        AtomicInteger totalCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();
        int effectiveBatchSize = Math.min(batchSize, MAX_BATCH_SIZE);

        return ParquetParser.parseInStream(file.getInputStream(), factory)
                .buffer(effectiveBatchSize)
                .concatMap(batch -> {
                    totalCounter.addAndGet(batch.size());
                    Flux<MatchSuggestion> suggestions = RequestMakerUtility
                            .convertResponsesToMatchSuggestions(Flux.fromIterable(batch), groupId);
                    return Mono.fromFuture(service.saveMatchSuggestions(suggestions, groupId))
                            .then(Mono.fromRunnable(() -> transactionTemplate.execute(status -> {
                                repository.incrementProcessed(jobId, batch.size());
                                return null;
                            })))
                            .doOnError(e -> {
                                errorCounter.addAndGet(batch.size());
                                log.warn("Batch failed, skipping {} matches: {}", batch.size(), e.getMessage());
                            })
                            .onErrorResume(e -> Mono.empty());
                })
                .then(Mono.fromRunnable(() -> transactionTemplate.execute(status -> {
                    int total = totalCounter.get();
                    repository.updateTotalRows(jobId, total);

                    int processed = repository.getProcessedRows(jobId);

                    if (processed < total - errorCounter.get()) {
                        failJob(jobId, groupId, "Some match suggestions failed", processed, total);
                    } else {
                        completeJob(jobId, groupId, total);
                    }
                    return null;
                })))
                .publishOn(Schedulers.fromExecutor(matchSuggestionsImportExecutor))
                .toFuture()
                .exceptionally(throwable -> {
                    log.error("Job {} failed: {}", jobId, throwable.getMessage(), throwable);
                    handleUnexpectedFailure(jobId, groupId, throwable);
                    return null;
                });
    }

    private void completeJob(UUID jobId, String groupId, int total) {
        repository.markCompleted(jobId);
        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.COMPLETED.name(), total, total, List.of(), List.of(), domainId);
        sendStatus(job);
    }

    private void failJob(UUID jobId, String groupId, String reason, int processed, int total) {
        repository.markFailed(jobId, reason);
        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.FAILED.name(), processed, total, List.of(), List.of(), domainId);
        sendStatus(job);
    }

    private void handleUnexpectedFailure(UUID jobId, String groupId, Throwable e) {
        repository.markFailed(jobId, "Unexpected error: " + e.getMessage());
        int processed = repository.getProcessedRows(jobId);
        int total = repository.getTotalRows(jobId);
        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.FAILED.name(), processed, total, List.of(), List.of(), domainId);
        sendStatus(job);
    }

    private void sendStatus(NodesTransferJobExchange job) {
        producer.sendMessage(TOPIC, StringConcatUtil.concatWithSeparator("-", domainId, job.getJobId().toString()), BasicUtility.stringifyObject(job));
    }
}