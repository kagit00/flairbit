package com.dating.flairbit.processor;


import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.dto.enums.GroupType;
import com.dating.flairbit.repo.UserRepository;
import com.dating.flairbit.utils.Constant;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersExprtProcessor {
    private final UserRepository userRepository;
    private final CostBasedUsersExportProcessor costBasedUsersExportProcessor;
    private final NonCostBasedUsersExportProcessor nonCostBasedUsersExportProcessor;
    private final MeterRegistry meterRegistry;

    @Value("${export.batch-size:1000}")
    private int batchSize;

    @Transactional(readOnly = true)
    public void processGroup(String groupId, String groupType, UUID domainId) {
        log.info("Starting export for group: {} (type: {})", groupId, groupType);
        long startTime = System.nanoTime();

        List<Object[]> raw = userRepository.findByGroupIdAndSentToMatchingServiceFalse(groupId);
        List<UserExportDTO> users = RequestMakerUtility.transformToUserExportDTO(raw);
        log.debug("Fetched {} users for group '{}'", users.size(), groupId);

        CompletableFuture<Void> future = processBatchForGroupType(groupId, groupType, users, domainId);
        future.join();

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Completed export for group '{}' in {} ms", groupId, durationMs);
        meterRegistry.timer("users_export_group_duration", Constant.GROUP_ID, groupId, Constant.GROUP_TYPE, groupType)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private CompletableFuture<Void> processBatchForGroupType(String groupId, String groupType, List<UserExportDTO> userDtos, UUID domainId) {
        try {
            if (GroupType.COST_BASED.name().equalsIgnoreCase(groupType)) {
                return costBasedUsersExportProcessor.processBatch(groupId, userDtos, domainId);
            } else if (GroupType.NON_COST_BASED.name().equalsIgnoreCase(groupType)) {
                return nonCostBasedUsersExportProcessor.processBatch(groupId, userDtos, domainId);
            } else {
                log.warn("Unknown group type '{}' for group '{}'. Skipping.", groupType, groupId);
                meterRegistry.counter("users_export_invalid_group_type", "groupId", groupId, "groupType", groupType).increment();
                return CompletableFuture.completedFuture(null);
            }
        } catch (Exception e) {
            log.error("Export failed for group '{}', type '{}': {}", groupId, groupType, e.getMessage());
            meterRegistry.counter("users_export_batch_failures", "groupId", groupId, "groupType", groupType).increment();
            return CompletableFuture.completedFuture(null);
        }
    }
}