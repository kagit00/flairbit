package com.dating.flairbit.processor;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.enums.GroupType;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.UserRepository;
import com.dating.flairbit.service.user.UsersExportService;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsersExportProceessor {
    private final UserRepository userRepository;
    private final UsersExportService userExportService;
    private final FlairBitProducer flairBitProducer;
    private static final String USERS_EXPORT = "flairbit-users";

    @Value("${export.batch-size}")
    private int batchSize;

    @Async("usersDumpExecutor")
    public void processGroup(String groupId, String groupType, UUID domainId) {
        log.info("Processing export for group: {} (type: {})", groupId, groupType);

        List<User> users = userRepository.findByGroupIdAndSentToMatchingServiceFalse(groupId);
        if (users.isEmpty()) {
            log.info("No new users for group '{}'", groupId);
            return;
        }

        try {
            List<List<User>> batches = partitionUsers(users);

            if (GroupType.COST_BASED.name().equalsIgnoreCase(groupType)) {
                processCostBasedGroup(groupId, users, domainId);
            } else if (GroupType.NON_COST_BASED.name().equalsIgnoreCase(groupType)) {
                processNonCostBasedGroup(groupId, batches, domainId);
            } else {
                log.warn("Unknown group type '{}' for group '{}'. Skipping.", groupType, groupId);
            }

        } catch (Exception e) {
            log.error("Export failed for group '{}', type '{}':", groupId, groupType, e);
        }
    }

    private void processCostBasedGroup(String groupId, List<User> users, UUID domainId) {
        List<List<User>> batches = partitionUsers(users);
        for (int i = 0; i < batches.size(); i++) {
            List<User> batch = batches.get(i);
            ExportedFile file = userExportService.exportCsv(batch, groupId, domainId);

            if (file.content().length == 0) {
                log.info("No matching profiles for group '{}', batch {}. Skipping export.", groupId, i);
                continue;
            }

            String batchFileName = String.format("%s_batch_%d_users.csv", groupId, i);
            NodeExchange payload = RequestMakerUtility.buildCostBasedNodes(groupId, file.content(), batchFileName, file.contentType(), domainId);
            flairBitProducer.sendMessage(
                    USERS_EXPORT,
                    StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                    BasicUtility.stringifyObject(payload)
            );
            log.info("Exported {} bytes for cost-based group '{}', batch {}", file.content().length, groupId, i);
        }
    }

    private void processNonCostBasedGroup(String groupId, List<List<User>> batches, UUID domainId) {
        for (int i = 0; i < batches.size(); i++) {
            List<User> batch = batches.get(i);
            List<String> usernames = userExportService.extractEligibleUsernames(batch, groupId);

            if (usernames.isEmpty()) {
                log.info("No matching profiles for non-cost-based group '{}', batch {}", groupId, i);
                continue;
            }

            NodeExchange payload = RequestMakerUtility.buildNonCostBasedNodesPayload(groupId, usernames, domainId);
            flairBitProducer.sendMessage(
                    USERS_EXPORT,
                    StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                    BasicUtility.stringifyObject(payload)
            );
            log.info("Sent {} users for non-cost-based group '{}', batch {}", usernames.size(), groupId, i);
        }
    }


    private List<List<User>> partitionUsers(List<User> users) {
        List<List<User>> batches = new ArrayList<>();
        for (int i = 0; i < users.size(); i += batchSize) {
            batches.add(users.subList(i, Math.min(i + batchSize, users.size())));
        }
        return batches;
    }
}
