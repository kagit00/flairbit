package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.GenderType;
import com.dating.flairbit.dto.enums.IntentType;
import com.dating.flairbit.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String name;
    private GenderType gender;
    private LocalDate dateOfBirth;
    private IntentType intent;
    private boolean dumpedToMatcher;
    private boolean profileComplete;
    private boolean readyForMatching;
    private boolean isNotificationEnabled;
    private String groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LifestyleResponse lifestyle;
    private PreferencesResponse preferences;
    private List<MediaFileResponse> mediaFiles;
    private LocationResponse location;
    private EducationResponse education;
    private ProfessionResponse profession;
    private String bio;
    private Set<Role> roles = new HashSet<>();
}
