package com.dating.flairbit.processor;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.service.user.UsersExportFormattingService;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CostBasedUsersExportProcessor {
    private final UsersExportFormattingService usersExportFormattingService;
    private final FlairBitProducer flairBitProducer;
    private static final String USERS_EXPORT = "flairbit-users";

    @Transactional
    public void processGroup(String groupId, List<UserExportDTO> userDtos, UUID domainId, int batchSize) {
        List<List<UserExportDTO>> batches = BasicUtility.partitionList(userDtos, batchSize);
        for (int i = 0; i < batches.size(); i++) {
            List<UserExportDTO> batch = batches.get(i);
            ExportedFile file = usersExportFormattingService.exportCsv(batch, groupId, domainId);

            if (Objects.isNull(file)) {
                log.info("No matching profiles for group '{}', batch {}. Skipping export.", groupId, i);
                continue;
            }

            String batchFileName = String.format("%s_batch_%d_users.csv", groupId, i);
            NodeExchange payload = RequestMakerUtility.buildCostBasedNodes(groupId, file.filePath(), batchFileName, file.contentType(), domainId);
            flairBitProducer.sendMessage(
                    USERS_EXPORT,
                    StringConcatUtil.concatWithSeparator("-", domainId.toString(), groupId),
                    BasicUtility.stringifyObject(payload)
            );
            log.info("Exported for cost-based group '{}', batch {}", groupId, i);
        }
    }
}
