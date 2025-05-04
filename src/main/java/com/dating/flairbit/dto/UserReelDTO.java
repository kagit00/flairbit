package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReelDTO {
    private UUID profileId;
    private List<MediaFileResponse> media;
    private Double score;
}
