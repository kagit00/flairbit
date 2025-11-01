package com.dating.flairbit.service.profile.prefrerences;

import com.dating.flairbit.dto.PreferencesRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Preferences;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.PreferencesRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreferencesUpdateServiceImpl implements PreferencesUpdateService {
    private final UserService userService;
    private final PreferencesRepository preferencesRepository;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    @CacheEvict(value = {"profileCache"}, key = "#email")
    public void createOrUpdatePreferences(String email, PreferencesRequest request, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (ObjectUtils.allNull(request)) throw new BadRequestException("Preferences request cannot be empty.");
        if (Objects.isNull(profile)) throw new BadRequestException("No profile found with intent "  + intent);

        Preferences preferences = profile.getPreferences() != null
                ? profile.getPreferences()
                : Preferences.builder().createdAt(DefaultValuesPopulator.getCurrentTimestamp()).build();

        if (!Objects.equals(request.getPreferredGenders(), preferences.getPreferredGenders())) {
            preferences.setPreferredGenders(request.getPreferredGenders());
        }

        if (!Objects.equals(request.getPreferredMinAge(), preferences.getPreferredMinAge())) {
            preferences.setPreferredMinAge(request.getPreferredMinAge());
        }

        if (!Objects.equals(request.getPreferredMaxAge(), preferences.getPreferredMaxAge())) {
            preferences.setPreferredMaxAge(request.getPreferredMaxAge());
        }

        if (!StringUtils.equalsIgnoreCase(request.getRelationshipType(), preferences.getRelationshipType())) {
            preferences.setRelationshipType(request.getRelationshipType());
        }

        if (!Objects.equals(request.getWantsKids(), preferences.getWantsKids())) {
            preferences.setWantsKids(request.getWantsKids());
        }

        if (!Objects.equals(request.getOpenToLongDistance(), preferences.getOpenToLongDistance())) {
            preferences.setOpenToLongDistance(request.getOpenToLongDistance());
        }

        preferences.setProfile(profile);
        preferences.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        preferencesRepository.save(preferences);
    }
}
