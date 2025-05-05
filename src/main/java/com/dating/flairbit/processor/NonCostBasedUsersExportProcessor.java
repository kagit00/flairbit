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

@Component
@RequiredArgsConstructor
@Slf4j
public class NonCostBasedUsersExportProcessor {
    private final UsersExportFormattingService userExportService;
    private final FlairBitProducer flairBitProducer;
    private static final String USERS_EXPORT = "flairbit-users";

    @Transactional
    public void processGroup(String groupId, List<List<UserExportDTO>> batches, UUID domainId) {
        for (int i = 0; i < batches.size(); i++) {
            List<UserExportDTO> batch = batches.get(i);
            List<String> refIds = userExportService.extractEligibleUsernames(batch, groupId);

            if (refIds.isEmpty()) {
                log.info("No matching profiles for non-cost-based group '{}', batch {}", groupId, i);
                continue;
            }

            NodeExchange payload = RequestMakerUtility.buildNonCostBasedNodesPayload(groupId, refIds, domainId);
            flairBitProducer.sendMessage(
                    USERS_EXPORT,
                    StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                    BasicUtility.stringifyObject(payload)
            );
            log.info("Sent {} users for non-cost-based group '{}', batch {}", refIds.size(), groupId, i);
        }
    }
}
