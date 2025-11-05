package com.dating.flairbit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    private UUID id;
    private String userEmail;
    private String displayName;
    private String headline;
    private String bio;

    private EducationResponse education;
    private ProfessionResponse profession;
    private LocationResponse location;
    private LifestyleResponse lifestyle;
    private PreferencesResponse preferences;

    private List<MediaFileResponse> mediaFiles;
}


