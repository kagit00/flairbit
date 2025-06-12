package com.dating.flairbit.service.importjob;

import com.dating.flairbit.dto.MatchSuggestionsExchange;

import java.util.concurrent.CompletableFuture;

public interface ImportJobService {
    CompletableFuture<Void> startMatchesImport(MatchSuggestionsExchange payload);
}
