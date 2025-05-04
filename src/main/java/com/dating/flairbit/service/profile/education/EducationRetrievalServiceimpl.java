package com.dating.flairbit.service.profile.education;

import com.dating.flairbit.dto.EducationResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EducationRetrievalServiceimpl implements EducationRetrievalService {
    private final UserService userService;
    private final ProfileProcessor profileProcessor;


    @Override
    @Transactional(readOnly = true)
    public EducationResponse getEducation(String email, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);
        return ResponseMakerUtility.buildEducation(profile.getEducation());
    }
}
