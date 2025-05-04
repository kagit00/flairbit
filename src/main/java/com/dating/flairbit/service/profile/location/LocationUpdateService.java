package com.dating.flairbit.service.profile.location;

import com.dating.flairbit.dto.LocationRequest;

public interface LocationUpdateService {
    void createOrUpdateLocation(String email, LocationRequest request, String intent);
}
