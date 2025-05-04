package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.GenderType;
import com.dating.flairbit.dto.enums.IntentType;
import com.dating.flairbit.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {
    @NotBlank(message = "Display name cannot be blank")
    private String displayName;

    @NotBlank(message = "Headline cannot be blank")
    private String headline;

    @NotBlank(message = "Bio cannot be blank")
    private String bio;

    @NotBlank(message = "Intent is required")
    @ValidEnum(enumClass = IntentType.class, message = "Invalid intent")
    private String intent;

    @NotBlank(message = "Gender is required")
    @ValidEnum(enumClass = GenderType.class, message = "Invalid gender")
    private String gender;

    @NotNull(message = "Date of birth cannot be null")
    private LocalDate dob;

    private boolean sentToMatchingService;
    private boolean profileComplete;
}