package com.dating.flairbit.processor;

import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.repo.UserMatchStateRepository;
import com.dating.flairbit.repo.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersExportJobStatusProcessor {

    private final UserRepository userRepository;
    private final UserMatchStateRepository userMatchStateRepository;
    private final TransactionTemplate transactionTemplate;
    private final RetryTemplate retryTemplate;
    private final MeterRegistry meterRegistry;
    private static final int BATCH_SIZE = 25000;

    @Async("usersExportExecutor")
    public CompletableFuture<Void> processJobStatus(NodesTransferJobExchange job) {
        long startTime = System.nanoTime();
        UUID jobId = job.getJobId();
        String groupId = job.getGroupId();
        String status = job.getStatus();
        Integer processedNodes = job.getProcessed();
        Integer totalNodes = job.getTotal();
        List<String> successList = new ArrayList<>(job.getSuccessList());
        List<String> failedList = new ArrayList<>(job.getFailedList());

        log.info(
                "Received job status: jobId={}, groupId={}, status={}, processedNodes={}, totalNodes={}, successCount={}, failedCount={}",
                jobId, groupId, status, processedNodes, totalNodes, successList.size(), failedList.size()
        );

        try {
            if (StringUtils.isEmpty(status)) {
                log.error("Missing 'status' field in payload: {}", job);
                meterRegistry.counter("job_status_errors", "groupId", groupId, "jobId", jobId.toString()).increment();
                return CompletableFuture.completedFuture(null);
            }

            if (JobStatus.COMPLETED.name().equalsIgnoreCase(status) && !successList.isEmpty()) {
                markSuccessfulUsers(jobId, successList);
            }

            if (!failedList.isEmpty()) {
                log.warn("Failed usernames for jobId={}: {}", jobId, failedList);
                meterRegistry.counter("job_status_failed_users", "groupId", groupId, "jobId", jobId.toString()).increment(failedList.size());
            }

            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            meterRegistry.timer("job_status_processing_duration", "groupId", groupId, "jobId", jobId.toString())
                    .record(durationMs, TimeUnit.MILLISECONDS);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to process job status for jobId={}: {}", jobId, e.getMessage(), e);
            meterRegistry.counter("job_status_errors", "groupId", groupId, "jobId", jobId.toString()).increment();
            return CompletableFuture.failedFuture(e);
        }
    }

    private void markSuccessfulUsers(UUID jobId, List<String> successList) {
        List<List<String>> batches = ListUtils.partition(successList, BATCH_SIZE);
        for (List<String> batch : batches) {
            retryTemplate.execute(context -> {
                transactionTemplate.execute(status -> {
                    List<UUID> userIds = userRepository.findIdsByUsernames(batch)
                            .stream()
                            .sorted()
                            .toList();

                    if (userIds.isEmpty()) {
                        log.warn("No userIds found for usernames: {} in jobId={}", batch, jobId);
                        meterRegistry.counter("job_status_empty_users", "jobId", jobId.toString()).increment();
                        return null;
                    }

                    userMatchStateRepository.markSentToMatchingService(userIds);
                    log.info("Marked {} users as sent to matching service for jobId={}", userIds.size(), jobId);
                    meterRegistry.counter("job_status_marked_users", "jobId", jobId.toString()).increment(userIds.size());
                    return null;
                });
                return null;
            });
        }
    }
}