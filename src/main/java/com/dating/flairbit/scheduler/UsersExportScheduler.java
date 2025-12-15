package com.dating.flairbit.scheduler;


import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MatchingGroupConfig;
import com.dating.flairbit.service.user.UsersExportService;
import com.dating.flairbit.repo.MatchingGroupConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class UsersExportScheduler {

    private final MatchingGroupConfigRepository groupConfigRepository;
    private final UsersExportService usersExportService;

    @Value("${domain-id}")
    private String domainId;

    @PostConstruct
    public void validateDomainId() {
        try {
            UUID.fromString(domainId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid domain-id: not a valid UUID -> " + domainId);
        }
    }

    @Scheduled(cron = "0 59 20 * * *", zone = "Asia/Kolkata")
    public void scheduledExportJob() {
        List<MatchingGroupConfig> groupConfigs = groupConfigRepository.findAll();
        log.info("Starting export for {} active groups", groupConfigs.size());

        CompletableFuture<?>[] futures = groupConfigs.stream()
                .map(config -> usersExportService.processGroup(config.getId(), config.getType(), UUID.fromString(domainId))
                        .exceptionally(throwable -> {
                            log.error("Failed to process group '{}': {}", config.getId(), throwable.getMessage());
                            return null;
                        }))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        log.info("Completed export scheduling for all groups");
    }
}