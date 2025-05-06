package com.dating.flairbit.service.match;

import com.dating.flairbit.processor.MatchSuggestionsImportJobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class MatchSuggestionsImportJobService {
    private final MatchSuggestionsImportJobProcessor matchSuggestionsImportJobProcessor;

    @Async
    public void processImportedMatchSuggestions(UUID jobId, MultipartFile file, String groupId, int batchSize) {
        matchSuggestionsImportJobProcessor.process(jobId, file, groupId, batchSize);
    }
}
