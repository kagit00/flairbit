package com.dating.flairbit.service.match.suggestions;


import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.repo.MatchSuggestionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSuggestionsStorageServiceImpl implements MatchSuggestionsStorageService {
    private final MatchSuggestionsRepository matchSuggestionsRepository;

    @Override
    @Transactional
    public void saveMatchSuggestions(List<MatchSuggestion> matchSuggestions) {
        List<MatchSuggestion> deduplicated = matchSuggestions.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                m -> m.getParticipantId() + "|" + m.getMatchedParticipantId() + "|" + m.getGroupId() + "|" + m.getMatchSuggestionType(),
                                Function.identity(),
                                (existing, duplicate) -> existing
                        ),
                        map -> new ArrayList<>(map.values())
                ));

        matchSuggestionsRepository.saveAll(deduplicated);
    }

    @Override
    @Cacheable(value = "matchSuggestionsCache", key = "#participantUsername" + "_matches_" + "#groupId")
    @Transactional(readOnly = true)
    public List<MatchSuggestion> retrieveMatchSuggestions(String participantUsername, String groupId) {
        return matchSuggestionsRepository.findFilteredSuggestions(participantUsername, groupId);
    }
}
