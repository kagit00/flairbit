package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.UserMatchStateDTO;
import com.dating.flairbit.models.Profile;

public interface UserMatchStateService {
    UserMatchStateDTO retrieveUserMatchState(String email);
    void createOrUpdateUserMatchState(Profile profile, UserMatchStateDTO request);
}
