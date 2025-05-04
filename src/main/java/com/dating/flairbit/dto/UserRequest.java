package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.GenderType;
import com.dating.flairbit.dto.enums.IntentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRequest {
    @NotBlank
    @Size(max = 50)
    private String username;
    @NotBlank
    private String password;
    @Size(max = 100)
    private String name;
    private GenderType gender;
    private LocalDate dateOfBirth;
    private String groupId;
    private IntentType intent;
    private Boolean isNotificationEnabled;
    private LifestyleRequest lifestyle;
    private PreferencesRequest preferences;
    private LocationRequest location;
    private ProfessionRequest profession;
    private EducationRequest education;
    private String bio;
}

