package com.dating.flairbit.config.factory;

import com.dating.flairbit.dto.MatchSuggestionDTO;
import com.dating.flairbit.dto.enums.NodeType;
import java.util.Map;

public class MatchInfoResponseFactory implements ResponseFactory<MatchSuggestionDTO> {

    @Override
    public MatchSuggestionDTO createResponse(NodeType type, String referenceId, Map<String, String> metadata, String groupId) {
        return MatchSuggestionDTO.builder()
                .matchedParticipantId(metadata.get("matched_reference_id"))
                .compatibilityScore(Double.valueOf(metadata.get("compatibility_score")))
                .participantId(referenceId)
                .groupId(groupId)
                .build();
    }
}