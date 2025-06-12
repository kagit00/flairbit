package com.dating.flairbit.processor.reels.interaction;

import com.dating.flairbit.dto.ReelInteractionDTO;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.ReelInteraction;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.ReelInteractionRepository;
import com.dating.flairbit.service.profile.media.MediaRetrievalService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.sun.jersey.api.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReelInteractionProcessor {

    private final MediaRetrievalService mediaRetrievalService;
    private final ReelInteractionRepository reelInteractionRepository;
    private final UserService userService;

    public CompletableFuture<Object> processReelInteractionRecording(ReelInteractionDTO interaction) {
        log.info("Processing reel interaction for userEmail={}, reelId={}, interactionType={}",
                interaction.getUserEmail(), interaction.getReelId(), interaction.getInteractionType());

        User user;
        try {
            user = userService.getUserByEmail(interaction.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to retrieve user for email={}: {}", interaction.getUserEmail(), e.getMessage());
            return CompletableFuture.failedFuture(new NotFoundException("User not found for email: " + interaction.getUserEmail()));
        }

        return mediaRetrievalService.getMediaFileById(interaction.getReelId())
                .thenCompose(mediaFile -> {
                    try {
                        ReelInteraction reelInteraction = RequestMakerUtility.getReelInteraction(
                                user, interaction.getInteractionType(), mediaFile);
                        reelInteractionRepository.save(reelInteraction);
                        log.info("Successfully saved reel interaction for reelId={}", interaction.getReelId());
                        return CompletableFuture.completedFuture(null); // Return a completed Void future
                    } catch (Exception e) {
                        log.error("Failed to save reel interaction for reelId={}: {}",
                                interaction.getReelId(), e.getMessage());
                        return CompletableFuture.failedFuture(
                                new InternalServerErrorException("Failed to save reel interaction: " + e.getMessage()));
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Failed to retrieve media file for reelId={}: {}",
                            interaction.getReelId(), throwable.getMessage());
                    return CompletableFuture.failedFuture(
                            new BadRequestException("Media file not found for reelId: " + interaction.getReelId()));
                });
    }
}