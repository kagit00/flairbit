package com.dating.flairbit.processor;

import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.repo.UserMatchStateRepository;
import com.dating.flairbit.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersExportJobStatusProcessor {

    private final UserRepository userRepository;
    private final UserMatchStateRepository userMatchStateRepository;


    public void processJobStatus(NodesTransferJobExchange job) {
        UUID jobId = job.getJobId();
        String groupId = job.getGroupId();
        String status = job.getStatus();
        Integer processedNodes = job.getProcessed();
        Integer totalNodes = job.getTotal();
        List<String> successList = job.getSuccessList();
        List<String> failedList = job.getFailedList();

        log.info(
                "Received job status: jobId={}, groupId={}, status={}, processedNodes={}, totalNodes={}, successCount={}, failedCount={}",
                jobId,
                groupId,
                status,
                processedNodes,
                totalNodes,
                successList.size(),
                failedList.size()
        );

        if (StringUtils.isEmpty(status)) {
            log.error("Missing 'status' field in payload: {}", job);
            return;
        }

        if (JobStatus.COMPLETED.name().equalsIgnoreCase(status) && !successList.isEmpty()) markSuccessfulUsers(jobId, successList);
        if (!failedList.isEmpty()) log.warn("Failed usernames for jobId={}: {}", jobId, failedList);
    }

    private void markSuccessfulUsers(UUID jobId, List<String> successList) {
        List<UUID> userIds = userRepository.findIdsByUsernames(successList);
        if (userIds.isEmpty()) {
            log.warn("No userIds found for usernames: {} in jobId={}", successList, jobId);
            return;
        }
        userMatchStateRepository.markSentToMatchingService(userIds);
        log.info("Marked {} users as sent to matching service for jobId={}", userIds.size(), jobId);
    }
}
