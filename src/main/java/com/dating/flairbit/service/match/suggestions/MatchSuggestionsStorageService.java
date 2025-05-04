package com.dating.flairbit.service.match.suggestions;

import com.dating.flairbit.models.MatchSuggestion;
import java.util.List;


public interface MatchSuggestionsStorageService {
    void saveMatches(List<MatchSuggestion> matches);
    List<MatchSuggestion> retrieveByParticipantIdAndGroupId(String participantId, String groupId);
}
