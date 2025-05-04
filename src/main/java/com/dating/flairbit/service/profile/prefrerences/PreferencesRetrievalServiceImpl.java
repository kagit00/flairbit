package com.dating.flairbit.service.profile.prefrerences;

import com.dating.flairbit.dto.PreferencesResponse;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreferencesRetrievalServiceImpl implements PreferencesRetrievalService {
    private final UserService userService;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences(String email, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);
        return ResponseMakerUtility.buildPreferences(profile.getPreferences());
    }
}
