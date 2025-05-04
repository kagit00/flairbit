package com.dating.flairbit.service.profile.prefrerences;

import com.dating.flairbit.dto.PreferencesResponse;

public interface PreferencesRetrievalService {
    PreferencesResponse getPreferences(String email, String intent);
}
