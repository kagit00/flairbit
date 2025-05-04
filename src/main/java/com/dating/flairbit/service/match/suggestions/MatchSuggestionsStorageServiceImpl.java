package com.dating.flairbit.service.match.suggestions;


import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.repo.MatchSuggestionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSuggestionsStorageServiceImpl implements MatchSuggestionsStorageService {
    private final MatchSuggestionsRepository matchSuggestionsRepository;

    @Override
    public void saveMatches(List<MatchSuggestion> matchSuggestions) {
        matchSuggestionsRepository.saveAll(matchSuggestions);
    }

    @Override
    @Cacheable(value = "matchSuggestionsCache", key = "#participantUsername" + "_matches")
    public List<MatchSuggestion>  retrieveByParticipantIdAndGroupId(String participantUsername, String groupId) {
        return matchSuggestionsRepository.findByParticipantIdAndGroupId(participantUsername, groupId);
    }
}
