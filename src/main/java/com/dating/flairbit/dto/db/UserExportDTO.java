package com.dating.flairbit.dto.db;

import java.time.LocalDate;
import java.util.UUID;


public record UserExportDTO(UUID userId, String username, UUID profileId, String displayName, String bio, String city,
                            Boolean smokes, Boolean drinks, String religion, Boolean wantsKids, String preferredGenders,
                            Integer preferredMinAge, Integer preferredMaxAge, String relationshipType,
                            Boolean openToLongDistance, String fieldOfStudy, String industry, String gender,
                            LocalDate dateOfBirth, String intent, Boolean readyForMatching, String groupId) {
}