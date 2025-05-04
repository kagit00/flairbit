package com.dating.flairbit.service.profile.prefrerences;

import com.dating.flairbit.dto.PreferencesRequest;

public interface PreferencesUpdateService {
    void createOrUpdatePreferences(String email, PreferencesRequest request, String intent);
}
