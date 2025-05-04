package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.MatchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequest {
    private String groupId;
    private MatchType matchType;
    private String industry;
    private boolean isRealTime;
}
