package com.dating.flairbit.service.profile.profession;

import com.dating.flairbit.dto.ProfessionResponse;

public interface ProfessionRetrievalService {
    ProfessionResponse getProfession(String email, String intent);
}
