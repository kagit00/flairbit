package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;



@Builder
@AllArgsConstructor
@Data
public class MatchSuggestionDTO {
    private String groupId;
    private String participantId;
    private String matchedParticipantId;
    private Double compatibilityScore;
}
