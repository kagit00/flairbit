package com.dating.flairbit.service.match;

import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.processor.MatchSuggestionsImportJobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchSuggestionsImportJobService {

    private final MatchSuggestionsImportJobProcessor matchSuggestionsImportJobProcessor;

    public CompletableFuture<Void> processImportedMatchSuggestions(UUID jobId, MultipartFile file, String groupId, int batchSize) {
        return CompletableFuture.runAsync(() -> {
                    try {
                        matchSuggestionsImportJobProcessor.process(jobId, file, groupId, batchSize);
                    } catch (IOException e) {
                        throw new InternalServerErrorException(e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Failed to process job {} for group {}: {}", jobId, groupId, throwable.getMessage());
                    throw new RuntimeException("Processing failed", throwable);
                });
    }
}