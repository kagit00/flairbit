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
public class ProfessionResponse {
    private UUID id;
    private String jobTitle;
    private String company;
    private String industry;
}
