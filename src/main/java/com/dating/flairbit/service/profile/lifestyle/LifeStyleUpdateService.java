package com.dating.flairbit.service.profile.lifestyle;

import com.dating.flairbit.dto.LifestyleRequest;

public interface LifeStyleUpdateService {
    void createOrUpdateLifestyle(String email, LifestyleRequest request, String intent);
}
