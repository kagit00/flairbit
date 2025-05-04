package com.dating.flairbit.utils;

import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.models.UserMatchState;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class ProfileUtils {

    private ProfileUtils() {
        throw new UnsupportedOperationException("unsupported operation");
    }

    public static Profile getProfileOrThrow(@NonNull User user, @NonNull String intent) {
        return user.getProfiles().stream()
                .filter(profile -> {
                    UserMatchState matchState = profile.getUserMatchState();
                    return matchState != null && intent.equalsIgnoreCase(matchState.getIntent());
                })
                .findFirst()
                .orElse(null);
    }
}