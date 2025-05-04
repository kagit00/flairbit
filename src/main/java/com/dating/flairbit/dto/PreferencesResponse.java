package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferencesResponse {
    private Set<String> preferredGenders = new HashSet<>();
    private Integer preferredMinAge;
    private Integer preferredMaxAge;
    private String relationshipType;
    private Boolean wantsKids;
    private Boolean openToLongDistance;
}
