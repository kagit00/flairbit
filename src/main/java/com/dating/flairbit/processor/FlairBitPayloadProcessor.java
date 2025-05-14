package com.dating.flairbit.processor;

import com.dating.flairbit.dto.MatchSuggestionsExchange;
import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.ReelInteractionDTO;
import com.dating.flairbit.processor.reels.interaction.ReelInteractionProcessor;
import com.dating.flairbit.service.importjob.ImportJobService;
import com.dating.flairbit.utils.basic.BasicUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlairBitPayloadProcessor {

    private final ImportJobService importJobService;
    private final UsersExportJobStatusProcessor usersExportJobStatusProcessor;
    private final ReelInteractionProcessor reelInteractionProcessor;

    public CompletableFuture<Void> processImportedMatchSuggestionsPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            MatchSuggestionsExchange parsedPayload = BasicUtility.safeParse(payload, MatchSuggestionsExchange.class);
            if (parsedPayload == null) {
                log.warn("Failed to parse imported matches payload: {}", payload);
                return null;
            }

            importJobService.startMatchesImport(parsedPayload);
            return null;
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to process imported matches payload", throwable);
            }
            return null;
        });
    }

    public CompletableFuture<Void> processUsersTransferJobStatusPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("Null or empty user transfer job status payload");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            NodesTransferJobExchange job = BasicUtility.safeParse(payload, NodesTransferJobExchange.class);
            if (job == null) {
                log.warn("Failed to parse users transfer job status payload: {}", payload);
                return null;
            }

            usersExportJobStatusProcessor.processJobStatus(job);
            return null;
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to process users transfer job status payload:", throwable);
            }
            return null;
        });
    }

    public CompletableFuture<Void> processReelInteractionPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            ReelInteractionDTO reelInteraction = BasicUtility.safeParse(payload, ReelInteractionDTO.class);
            if (reelInteraction == null) {
                log.warn("Failed to parse reel interaction payload: {}", payload);
                return null;
            }

            reelInteractionProcessor.processReelInteractionRecording(reelInteraction);
            return null;
        }).handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to process reel interaction payload: {}", payload, throwable);
            }
            return null;
        });
    }
}

