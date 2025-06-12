package com.dating.flairbit.service.match.suggestions;

import com.dating.flairbit.models.MatchSuggestion;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface MatchSuggestionsStorageService {
    CompletableFuture<Void> saveMatchSuggestions(Flux<MatchSuggestion> matchSuggestions, String groupId);
    CompletableFuture<List<MatchSuggestion>> retrieveMatchSuggestions(String participantId, String groupId);
}
