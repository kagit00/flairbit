package com.dating.flairbit.service.profile.location;

import com.dating.flairbit.dto.LocationResponse;

public interface LocationRetrievalService {
    LocationResponse getLocation(String email, String intent);
}
