package com.dating.flairbit.service.profile.education;

import com.dating.flairbit.dto.EducationRequest;

public interface EducationUpdateService {
    void createOrUpdateEducation(String email, EducationRequest educationRequest, String intent);
}