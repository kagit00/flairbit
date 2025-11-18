package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.UserMatchStateDTO;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.models.UserMatchState;
import com.dating.flairbit.repo.UserMatchStateRepository;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserMatchStateServiceImpl implements UserMatchStateService {

    private final UserMatchStateRepository userMatchStateRepository;
    private final UserService userService;

    @Override
    public UserMatchStateDTO retrieveUserMatchState(String email) {
        User user = userService.getUserByEmail(email);
        return user.getProfiles().stream()
                .map(Profile::getUserMatchState)
                .filter(Objects::nonNull)
                .findFirst()
                .map(ResponseMakerUtility::buildMatchState)
                .orElseThrow(() -> new BadRequestException("UserMatchState not found for user: " + email));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"profileCache"}, key = "#request.email")
    public void createOrUpdateUserMatchState(Profile profile, UserMatchStateDTO request) {
        if (Objects.isNull(profile)) throw new BadRequestException("Profile, request, or intent cannot be null.");
        if (ObjectUtils.allNull(request)) throw new BadRequestException("user match state payload cannot be empty.");

        UserMatchState matchState = profile.getUserMatchState();

        if (Objects.isNull(matchState)) {
            matchState = UserMatchState.builder().intent(request.getIntent()).gender(request.getGender()).profile(profile).build();
            profile.setUserMatchState(matchState);
            matchState.setCreatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        }

        matchState.setSentToMatchingService(request.isSentToMatchingService());
        matchState.setProfileComplete(request.isProfileComplete());
        matchState.setReadyForMatching(request.isReadyForMatching());
        matchState.setGroupId(request.getGroupId());

        if (!request.getIntent().equalsIgnoreCase(matchState.getIntent())) {
            matchState.setIntent(request.getIntent());
        }

        if (!Objects.equals(request.getDateOfBirth(), matchState.getDateOfBirth())) {
            matchState.setDateOfBirth(request.getDateOfBirth());
        }

        if (!Objects.equals(request.getLastMatchedAt(), matchState.getLastMatchedAt())) {
            matchState.setLastMatchedAt(request.getLastMatchedAt());
        }

        matchState.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        matchState.setProfile(profile);
        userMatchStateRepository.save(matchState);
    }
}
