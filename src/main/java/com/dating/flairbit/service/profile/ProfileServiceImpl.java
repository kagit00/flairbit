package com.dating.flairbit.service.profile;

import com.dating.flairbit.dto.ProfileRequest;
import com.dating.flairbit.dto.ProfileResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MatchingGroupConfig;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.ProfileJDBCRepository;
import com.dating.flairbit.repo.ProfileRepository;
import com.dating.flairbit.service.GroupConfigService;
import com.dating.flairbit.service.user.UserMatchStateService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserService userService;
    private final ProfileRepository profileRepository;
    private final UserMatchStateService userMatchStateService;
    private final GroupConfigService groupConfigService;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    @CacheEvict(value = {"profileCache"}, key = "#email")
    public Profile createOrUpdateProfile(String email, ProfileRequest profileRequest) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, profileRequest.getIntent());

        if (Objects.isNull(profile)) {
            profile = Profile.builder().build();
            profile.setCreatedAt(DefaultValuesPopulator.getCurrentTimestamp());
            profile.setUser(user);
        }

        String intent = profileRequest.getIntent();
        MatchingGroupConfig groupConfig = groupConfigService.getGroupConfig(intent);
        String groupId = groupConfig.getId();

        profile.setBio(profileRequest.getBio());
        profile.setDisplayName(profileRequest.getDisplayName());
        profile.setHeadline(profileRequest.getHeadline());
        profile.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());

        profileRepository.save(profile);
        userMatchStateService.createOrUpdateUserMatchState(
                profile,
                RequestMakerUtility.build(intent, groupId, profileRequest.getGender(), profileRequest.getDob(), email)
        );

        return profile;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);
        if (Objects.isNull(profile)) throw new BadRequestException("No profile with intent: " + intent);
        return ResponseMakerUtility.getFullProfileResponse(profile, email);
    }
}
