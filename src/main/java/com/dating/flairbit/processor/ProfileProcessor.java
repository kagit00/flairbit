package com.dating.flairbit.processor;

import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.ProfileJDBCRepository;
import com.dating.flairbit.repo.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileProcessor {
    private final ProfileJDBCRepository profileRepository;

    @Transactional
    @Cacheable(value = "profileCache", key = "#user.getId().toString() + '_' + #intent", unless = "#result == null")
    public Profile getProfile(User user, String intent) {
        return profileRepository.findByUserIdAndIntent(user.getId(), intent);
    }
}
