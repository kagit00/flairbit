package com.dating.flairbit.service.user;


import com.dating.flairbit.processor.UsersExprtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersExportService {
    private final UsersExprtProcessor usersExportProcessor;
    private final RetryTemplate retryTemplate;
    private final MeterRegistry meterRegistry;

    @Async("usersExportExecutor")
    public CompletableFuture<Void> processGroup(String groupId, String groupType, UUID domainId) {
        long startTime = System.nanoTime();
        log.info("Processing export for group: {} (type: {})", groupId, groupType);
        try {
            retryTemplate.execute(context -> {
                usersExportProcessor.processGroup(groupId, groupType, domainId);
                return null;
            });
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            meterRegistry.timer("users_export_duration", "groupId", groupId, "groupType", groupType)
                    .record(durationMs, TimeUnit.MILLISECONDS);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Export failed for group '{}', type '{}': {}", groupId, groupType, e.getMessage(), e);
            meterRegistry.counter("users_export_failures", "groupId", groupId, "groupType", groupType).increment();
            return CompletableFuture.failedFuture(new RuntimeException("Export failed for group: " + groupId, e));
        }
    }
}