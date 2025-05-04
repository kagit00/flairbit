package com.dating.flairbit.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationRequest {
    @Size(max = 100, message = "Degree must be at most 100 characters")
    private String degree;
    @Size(max = 200, message = "Institution must be at most 200 characters")
    private String institution;
    @Size(max = 100, message = "Field of study must be at most 100 characters")
    private String fieldOfStudy;
    private Integer graduationYear;
}
