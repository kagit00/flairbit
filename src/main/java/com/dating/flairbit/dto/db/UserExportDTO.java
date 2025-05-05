package com.dating.flairbit.dto.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserExportDTO {
    private final UUID userId;
    private final String username;
    private final UUID profileId;
    private final String displayName;
    private final String bio;
    private final String city;
    private final Boolean smokes;
    private final Boolean drinks;
    private final String religion;
    private final Boolean wantsKids;
    private final String preferredGenders;
    private final Integer preferredMinAge;
    private final Integer preferredMaxAge;
    private final String relationshipType;
    private final Boolean openToLongDistance;
    private final String fieldOfStudy;
    private final String industry;
    private final String gender;
    private final LocalDate dateOfBirth;
    private final String intent;
    private final Boolean readyForMatching;
    private final String groupId;
}