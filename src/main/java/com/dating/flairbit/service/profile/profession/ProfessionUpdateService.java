package com.dating.flairbit.service.profile.profession;

import com.dating.flairbit.dto.ProfessionRequest;

public interface ProfessionUpdateService {
    void createOrUpdateProfession(String email, ProfessionRequest request, String intent);
}
