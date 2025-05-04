package com.dating.flairbit.controller.profile.media.reels;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.ReelInteractionDTO;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;


@RestController
@RequestMapping("/users/{email}/media")
public class ReelInteractionController {

    private final FlairBitProducer flairBitProducer;

    public ReelInteractionController(FlairBitProducer flairBitProducer) {
        this.flairBitProducer = flairBitProducer;
    }

    @Value("${kafka.topics.reel-interaction-record:reel-interaction-record}")
    private String reelInteractionRecordTopic;

    @PutMapping(
            value = "/reel/{reelId}/{interactionType}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public void recordReelInteraction(@PathVariable UUID reelId, @PathVariable String interactionType, @PathVariable String email) {
        ReelInteractionDTO reelInteraction = ReelInteractionDTO.builder()
                .reelId(reelId)
                .interactionType(interactionType)
                .userEmail(email)
                .build();

        String messageKey = StringConcatUtil.concatWithSeparator("-", reelId.toString(), email, interactionType);
        flairBitProducer.sendMessage(reelInteractionRecordTopic, messageKey, BasicUtility.stringifyObject(reelInteraction));
    }
}
