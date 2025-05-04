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
public class EducationResponse {
    private UUID id;
    private String degree;
    private String institution;
    private String fieldOfStudy;
    private Integer graduationYear;
}
