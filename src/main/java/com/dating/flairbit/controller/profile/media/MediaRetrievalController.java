package com.dating.flairbit.controller.profile.media;


import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.dto.enums.ReelsSectionType;
import com.dating.flairbit.models.User;
import com.dating.flairbit.service.profile.media.MediaRetrievalService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/users/{email}/media")
@RequiredArgsConstructor
public class MediaRetrievalController {

    private final MediaRetrievalService mediaRetrievalService;
    private final UserService userService;

    @Transactional(readOnly = true)
    @GetMapping(
            value = "/matched-users-reels/{intent}/{sectionType}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<MediaFileResponse>> getUsersReels(
            @PathVariable String email,
            @PathVariable String intent,
            @PathVariable String sectionType,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor) {

        User user = userService.getUserByEmail(email);
        LocalDateTime cursorDateTime = (!Objects.isNull(cursor))
                ? LocalDateTime.parse(cursor)
                : DefaultValuesPopulator.getCurrentTimestamp();

        List<MediaFileResponse> reels;

        switch (ReelsSectionType.valueOf(sectionType.toUpperCase())) {
            case FOR_YOU -> reels = mediaRetrievalService.getMatchedUsersReels(user.getUsername(), cursorDateTime, limit, intent);
            case MOST_LIKED -> reels = mediaRetrievalService.getMostLikedReels(cursorDateTime, limit, intent);
            case MOST_VIEWED -> reels = mediaRetrievalService.getMostViewedReels(cursorDateTime, limit, intent);
            default -> reels = Collections.emptyList();
        }

        return ResponseEntity.ok(reels);
    }


    @Transactional(readOnly = true)
    @GetMapping(value = "/user-reels/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MediaFileResponse>> getReels(@PathVariable String email, @PathVariable String intent) {
        return ResponseEntity.ok(mediaRetrievalService.getUserReels(email, intent));
    }
}
