package com.dating.flairbit.scheduler;


import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MatchingGroupConfig;
import com.dating.flairbit.processor.UsersExportProceessor;
import com.dating.flairbit.repo.MatchingGroupConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class UsersExportScheduler {

    private final MatchingGroupConfigRepository groupConfigRepository;
    private final UsersExportProceessor usersExportProceessor;

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

    @Scheduled(cron = "${export.cron-schedule}")
    @Transactional
    public void scheduledExportJob() {
        List<MatchingGroupConfig> groupConfigs = groupConfigRepository.findAll();
        log.info("Starting export for active groups: {}", groupConfigs);
        groupConfigs.forEach(
                config -> usersExportProceessor.processGroup(config.getId(), config.getType(), UUID.fromString(domainId))
        );
    }
}