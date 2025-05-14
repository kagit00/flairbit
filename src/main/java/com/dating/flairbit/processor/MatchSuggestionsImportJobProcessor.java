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
import com.dating.flairbit.utils.db.BatchUtils;
import com.dating.flairbit.utils.media.csv.CsvParser;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchSuggestionsImportJobProcessor {
    private static final String MATCH_SUGGESTIONS_TRANSFER_JOB_STATUS_TOPIC = "flairbit-match-suggestions-transfer-job-status-retrieval";

    private final MatchSuggestionsImportJobRepository matchSuggestionsImportJobRepository;
    private final MatchSuggestionsStorageService matchSuggestionsStorageService;
    private final ResponseFactory<MatchSuggestionDTO> matchSuggestionDTOResponseFactory;
    private final FlairBitProducer flairBitProducer;

    @Value("${domain-id}")
    private String domainId;

    @Transactional
    public void process(UUID jobId, MultipartFile file, String groupId, int batchSize) {
        try {
            log.info("Job {} started for groupId={}, file name={}, size={} bytes", jobId, groupId, file.getOriginalFilename(), file.getSize());
            matchSuggestionsImportJobRepository.updateStatus(jobId, JobStatus.PROCESSING);

            AtomicInteger totalCounter = new AtomicInteger();
            List<String> success = new ArrayList<>();
            List<String> failed = new ArrayList<>();

            try (InputStream gzipStream = new GZIPInputStream(file.getInputStream())) {
                CsvParser.parseInBatches(gzipStream, matchSuggestionDTOResponseFactory, batch -> {
                    totalCounter.addAndGet(batch.size());
                    List<MatchSuggestion> matchSuggestions = RequestMakerUtility.convertResponsesToMatchSuggestions(batch, groupId);
                    if (matchSuggestions.isEmpty()) return;

                    BatchUtils.processInBatches(matchSuggestions, batchSize, subBatch -> {
                        try {
                            matchSuggestionsStorageService.saveMatchSuggestions(subBatch);
                            subBatch.forEach(m -> success.add(m.getParticipantId()));
                            matchSuggestionsImportJobRepository.incrementProcessed(jobId, subBatch.size());
                        } catch (Exception e) {
                            subBatch.forEach(m -> failed.add(m.getParticipantId()));
                            log.warn("Sub-batch failed, skipping {} match: {}", subBatch.size(), e.getMessage());
                        }
                    });
                });
            }

            int total = totalCounter.get();
            matchSuggestionsImportJobRepository.updateTotalRows(jobId, total);

            if (!failed.isEmpty()) {
                failJob(jobId, groupId, "Some match suggestions failed during saving.", success, failed, total);
            } else {
                completeJob(jobId, groupId, success, total);
            }

        } catch (Exception e) {
            log.error("Job {} failed: {}", jobId, e.getMessage(), e);
            handleUnexpectedFailure(jobId, groupId, e);
        }
    }

    private void completeJob(UUID jobId, String groupId, List<String> success, int total) {
        matchSuggestionsImportJobRepository.markCompleted(jobId);

        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.COMPLETED.name(), total, total, success, List.of(), domainId);
        sendStatus(job);
    }

    private void failJob(UUID jobId, String groupId, String reason, List<String> success, List<String> failed, int total) {
        matchSuggestionsImportJobRepository.markFailed(jobId, reason);

        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.FAILED.name(), total, total, success, List.of(), domainId);
        sendStatus(job);
    }

    private void handleUnexpectedFailure(UUID jobId, String groupId, Exception e) {
        matchSuggestionsImportJobRepository.markFailed(jobId, "Unexpected error: " + e.getMessage());

        int processed = matchSuggestionsImportJobRepository.getProcessedRows(jobId);
        int total = matchSuggestionsImportJobRepository.getTotalNodes(jobId);

        NodesTransferJobExchange job = RequestMakerUtility.buildImportJobReq(jobId, groupId, JobStatus.FAILED.name(), processed, total, List.of(), List.of(), domainId);
        sendStatus(job);
    }

    private void sendStatus(NodesTransferJobExchange job) {
        flairBitProducer.sendMessage(
                MATCH_SUGGESTIONS_TRANSFER_JOB_STATUS_TOPIC,
                StringConcatUtil.concatWithSeparator("-", domainId, job.getJobId().toString()),
                BasicUtility.stringifyObject(job)
        );
    }
}
