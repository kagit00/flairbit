package com.dating.flairbit.service;

import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MatchingGroupConfig;
import com.dating.flairbit.repo.MatchingGroupConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupConfigService {
    private final MatchingGroupConfigRepository groupConfigRepository;

    @Cacheable(value = "groupCache", key = "'group_id' + '_' + #intent")
    public MatchingGroupConfig getGroupConfig(String intent) {
        return groupConfigRepository.findByIntentAndActiveTrue(intent).orElseThrow(
                () -> new BadRequestException("No group against intent: " + intent)
        );
    }
}
