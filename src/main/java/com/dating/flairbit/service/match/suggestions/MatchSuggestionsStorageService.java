package com.dating.flairbit.service.match.suggestions;

import com.dating.flairbit.models.MatchSuggestion;
import java.util.List;


public interface MatchSuggestionsStorageService {
    void saveMatchSuggestions(List<MatchSuggestion> matchSuggestions);
    List<MatchSuggestion> retrieveMatchSuggestions(String participantId, String groupId);
}
