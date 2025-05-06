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

@Slf4j
@Component
@RequiredArgsConstructor
public class FlairBitPayloadProcessor {

    private final ImportJobService importJobService;
    private final UsersExportJobStatusProcessor usersExportJobStatusProcessor;
    private final ReelInteractionProcessor reelInteractionProcessor;

    public void processImportedMatchesPayload(String payload) {
        MatchSuggestionsExchange parsedPayload = BasicUtility.safeParse(payload, MatchSuggestionsExchange .class);
        if (Objects.isNull(parsedPayload)) return;
        importJobService.startMatchesImport(parsedPayload);
    }

    public void processUsersTransferJobStatusPayload(String payload) {
        NodesTransferJobExchange job = BasicUtility.safeParse(payload, NodesTransferJobExchange.class);
        if (Objects.isNull(job)) return;
        usersExportJobStatusProcessor.processJobStatus(job);
    }

    @Async
    public void processReelInteractionPayload(String payload) {
        ReelInteractionDTO reelInteraction = BasicUtility.safeParse(payload, ReelInteractionDTO.class);
        if (Objects.isNull(reelInteraction)) return;
        reelInteractionProcessor.processReelInteractionRecording(reelInteraction);
    }
}
