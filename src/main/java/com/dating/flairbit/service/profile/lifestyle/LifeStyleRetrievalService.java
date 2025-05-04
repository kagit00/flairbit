package com.dating.flairbit.service.profile.lifestyle;

import com.dating.flairbit.dto.LifestyleResponse;

public interface LifeStyleRetrievalService {
    LifestyleResponse getLifestyle(String email, String intent);
}
