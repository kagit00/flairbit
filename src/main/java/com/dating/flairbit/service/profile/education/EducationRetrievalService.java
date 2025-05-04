package com.dating.flairbit.service.profile.education;

import com.dating.flairbit.dto.EducationResponse;

public interface EducationRetrievalService {
    EducationResponse getEducation(String email, String intent);
}