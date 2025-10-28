package com.dating.flairbit.processor;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.service.user.UsersExportFormattingService;
import com.dating.flairbit.utils.Constant;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class CostBasedUsersExportProcessor {
    private final UsersExportFormattingService usersExportFormattingService;
    private final FlairBitProducer flairBitProducer;
    private final RetryTemplate retryTemplate;
    private final MeterRegistry meterRegistry;
    private static final String USERS_EXPORT = "flairbit-users";

    public CompletableFuture<Void> processBatch(String groupId, List<UserExportDTO> batch, UUID domainId) {
        if (isEmptyBatch(batch)) {
            log.info("Empty batch for group '{}', Skipping export.", groupId);
            return CompletableFuture.completedFuture(null);
        }

        long startTime = System.nanoTime();
        return usersExportFormattingService.exportCsv(batch, groupId, domainId)
                .thenCompose(file -> {
                    if (file == null) {
                        log.info("No matching profiles for group '{}'. Skipping export.", groupId);
                        return CompletableFuture.completedFuture(null);
                    }

                    String batchFileName = String.format("%s_batch_users.csv", groupId);
                    NodeExchange payload = RequestMakerUtility.buildCostBasedNodes(
                            groupId, file.filePath(), batchFileName, file.contentType(), domainId
                    );

                    retryTemplate.execute(context -> {
                        log.info(BasicUtility.stringifyObject(payload));
                        flairBitProducer.sendMessage(
                                USERS_EXPORT,
                                StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                                BasicUtility.stringifyObject(payload)
                        );
                        return null;
                    });

                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                    log.info("Exported {} users for cost-based group '{}' in {} ms", batch.size(), groupId, durationMs);
                    meterRegistry.timer("users_export_batch_duration", Constant.GROUP_ID, groupId).record(durationMs, TimeUnit.MILLISECONDS);
                    meterRegistry.counter("users_export_batch_processed", "groupId", groupId).increment(batch.size());
                    return CompletableFuture.completedFuture(null);
                })
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to process batch for group '{}', {}", groupId, throwable.getMessage(), throwable);
                        meterRegistry.counter("users_export_batch_failures", "groupId", groupId).increment();
                    }
                    return null;
                });
    }

    private boolean isEmptyBatch(List<UserExportDTO> batch) {
        return batch == null || batch.isEmpty();
    }
}