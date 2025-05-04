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
public class ProfessionRequest {
    @Size(max = 100, message = "Job title must be at most 100 characters")
    private String jobTitle;
    @Size(max = 200, message = "Company must be at most 200 characters")
    private String company;
    @Size(max = 100, message = "Industry must be at most 100 characters")
    private String industry;
}
