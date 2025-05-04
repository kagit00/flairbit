package com.dating.flairbit.service.profile.profession;

import com.dating.flairbit.dto.ProfessionResponse;
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
public class ProfessionRetrievalServiceImpl implements ProfessionRetrievalService {
    private final UserService userService;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional(readOnly = true)
    public ProfessionResponse getProfession(String email, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);
        return ResponseMakerUtility.buildProfession(profile.getProfession());
    }
}
