package com.dating.flairbit.processor;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.models.User;
import com.dating.flairbit.service.user.UsersExportFormattingService;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonCostBasedUsersExportProcessor {
    private final UsersExportFormattingService userExportService;
    private final FlairBitProducer flairBitProducer;
    private static final String USERS_EXPORT = "flairbit-users";

    public CompletableFuture<Void> processBatch(String groupId, List<UserExportDTO> batch, UUID domainId) {
        if (isEmptyBatch(batch)) {
            log.info("Empty batch for group '{}'. Skipping export.", groupId);
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> userExportService.extractEligibleUsernames(batch, groupId))
                .thenCompose(refIds -> {
                    if (refIds.isEmpty()) {
                        log.info("No matching profiles for non-cost-based group '{}'", groupId);
                        return CompletableFuture.completedFuture(null);
                    }

                    NodeExchange payload = RequestMakerUtility.buildNonCostBasedNodesPayload(groupId, refIds, domainId);
                    flairBitProducer.sendMessage(
                            USERS_EXPORT,
                            StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                            BasicUtility.stringifyObject(payload)
                    );
                    log.info("Sent {} users for non-cost-based group '{}'", refIds.size(), groupId);
                    return CompletableFuture.completedFuture(null);
                })
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to process batch for group '{}': {}", groupId, throwable.getMessage(), throwable);
                    }
                    return null;
                });
    }

    private boolean isEmptyBatch(List<UserExportDTO> batch) {
        return batch == null || batch.isEmpty();
    }
}