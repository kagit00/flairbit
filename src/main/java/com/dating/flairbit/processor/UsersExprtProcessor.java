package com.dating.flairbit.processor;


import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.dto.enums.GroupType;
import com.dating.flairbit.repo.UserRepository;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class UsersExprtProcessor {
    private final UserRepository userRepository;
    private final CostBasedUsersExportProcessor costBasedUsersExportProcessor;
    private final NonCostBasedUsersExportProcessor nonCostBasedUsersExportProcessor;

    @Value("${export.batch-size}")
    private int batchSize;

    @Transactional
    public void processGroup(String groupId, String groupType, UUID domainId) {
        log.info("Processing export for group: {} (type: {})", groupId, groupType);

        int page = 0;
        Pageable pageable = PageRequest.of(page, batchSize);
        Page<UserExportDTO> userPage;

        do {
            Page<Object[]> results = userRepository.findByGroupIdAndSentToMatchingServiceFalse(groupId, pageable);
            userPage = RequestMakerUtility.transformToUserExportDTO(results);
            List<UserExportDTO> userDtos = userPage.getContent();

            if (userDtos.isEmpty()) {
                log.info("No new users for group '{}', page {}", groupId, page);
                break;
            }

            try {
                if (GroupType.COST_BASED.name().equalsIgnoreCase(groupType)) {
                    costBasedUsersExportProcessor.processGroup(groupId, userDtos, domainId, batchSize);
                } else if (GroupType.NON_COST_BASED.name().equalsIgnoreCase(groupType)) {
                    nonCostBasedUsersExportProcessor.processGroup(groupId, List.of(userDtos), domainId);
                } else {
                    log.warn("Unknown group type '{}' for group '{}'. Skipping.", groupType, groupId);
                }
            } catch (Exception e) {
                log.error("Export failed for group '{}', type '{}', page {}:", groupId, groupType, page, e);
            }

            pageable = userPage.nextPageable();
            page++;
        } while (userPage.hasNext());

        log.info("Completed export for group '{}'", groupId);
    }
}
