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
public class LocationRequest {
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;
    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;
    private Double latitude;
    private Double longitude;
}
