package com.dating.flairbit.service.profile;

import com.dating.flairbit.dto.ProfileRequest;
import com.dating.flairbit.dto.ProfileResponse;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;

import java.util.UUID;


public interface ProfileService {
    Profile createOrUpdateProfile(String email, ProfileRequest profileRequest);
    ProfileResponse getProfile(String email, String intent);
}
