package com.dating.flairbit.service.importjob;

import com.dating.flairbit.dto.MatchSuggestionsExchange;

public interface ImportJobService {
    void startMatchesImport(MatchSuggestionsExchange payload);
}
