package com.dating.flairbit.processor.reels.interaction;

import com.dating.flairbit.dto.ReelInteractionDTO;
import com.dating.flairbit.models.MediaFile;
import com.dating.flairbit.models.ReelInteraction;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.ReelInteractionRepository;
import com.dating.flairbit.service.profile.media.MediaRetrievalService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReelInteractionProcessor {

    private final MediaRetrievalService mediaRetrievalService;
    private final ReelInteractionRepository reelInteractionRepository;
    private final UserService userService;

    public void processReelInteractionRecording(ReelInteractionDTO interaction) {
        MediaFile reel = mediaRetrievalService.getMediaFileById(interaction.getReelId());
        User user = userService.getUserByEmail(interaction.getUserEmail());
        ReelInteraction reelInteraction = RequestMakerUtility.getReelInteraction(user, interaction.getInteractionType(), reel);
        reelInteractionRepository.save(reelInteraction);
    }
}
