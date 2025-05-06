package com.dating.flairbit.service.user;


import com.dating.flairbit.processor.UsersExprtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UsersExportService {
    private final UsersExprtProcessor usersExprtProcessor;

    @Async("usersDumpExecutor")
    public void processGroup(String groupId, String groupType, UUID domainId) {
        log.info("Processing export for group: {} (type: {})", groupId, groupType);
        try {
            usersExprtProcessor.processGroup(groupId, groupType, domainId);
        } catch (Exception e) {
            log.error("Export failed for group '{}', type '{}':", groupId, groupType, e);
        }
    }
}
