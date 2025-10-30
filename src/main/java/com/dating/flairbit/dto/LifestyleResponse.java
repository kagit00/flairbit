package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifestyleResponse {
    private UUID id;
    private String occupation;
    private String education;
    private String religion;
    private String caste;
    private Boolean smokes;
    private Boolean drinks;
}
