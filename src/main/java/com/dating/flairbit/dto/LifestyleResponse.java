package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifestyleResponse {
    private String occupation;
    private String education;
    private String religion;
    private String caste;
    private Boolean smokes;
    private Boolean drinks;
}
