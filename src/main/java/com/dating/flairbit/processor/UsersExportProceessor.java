package com.dating.flairbit.processor;


import com.dating.flairbit.service.user.UsersExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class UsersExportProceessor {
    private final UsersExportService usersExportService;

    @Async("usersDumpExecutor")
    public void processGroup(String groupId, String groupType, UUID domainId) {
        log.info("Processing export for group: {} (type: {})", groupId, groupType);
        try {
            usersExportService.processGroup(groupId, groupType, domainId);
        } catch (Exception e) {
            log.error("Export failed for group '{}', type '{}':", groupId, groupType, e);
        }
    }
}
