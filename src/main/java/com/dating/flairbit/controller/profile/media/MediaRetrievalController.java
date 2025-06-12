package com.dating.flairbit.controller.profile.media;


import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.dto.enums.ReelsSectionType;
import com.dating.flairbit.models.User;
import com.dating.flairbit.service.profile.media.MediaRetrievalService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/users/{email}/media")
@RequiredArgsConstructor
public class MediaRetrievalController {

    private final MediaRetrievalService mediaRetrievalService;
    private final UserService userService;

    @GetMapping(
            value = "/matched-users-reels/{intent}/{sectionType}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public CompletableFuture<ResponseEntity<List<MediaFileResponse>>> getUsersReels(
            @PathVariable String email,
            @PathVariable String intent,
            @PathVariable String sectionType,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor) {

        User user = userService.getUserByEmail(email); // Keep this synchronous as it’s a simple lookup
        LocalDateTime cursorDateTime = (!Objects.isNull(cursor))
                ? LocalDateTime.parse(cursor)
                : DefaultValuesPopulator.getCurrentTimestamp();

        CompletableFuture<List<MediaFileResponse>> reelsFuture;
        try {
            switch (ReelsSectionType.valueOf(sectionType.toUpperCase())) {
                case FOR_YOU:
                    reelsFuture = mediaRetrievalService.getMatchedUsersReels(user.getUsername(), cursorDateTime, limit, intent);
                    break;
                case MOST_LIKED:
                    reelsFuture = mediaRetrievalService.getMostLikedReels(cursorDateTime, limit, intent);
                    break;
                case MOST_VIEWED:
                    reelsFuture = mediaRetrievalService.getMostViewedReels(cursorDateTime, limit, intent);
                    break;
                default:
                    reelsFuture = CompletableFuture.completedFuture(Collections.emptyList());
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid sectionType
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(Collections.emptyList())
            );
        }

        return reelsFuture
                .thenApply(reels -> ResponseEntity.ok(reels))
                .exceptionally(throwable -> {
                    log.error("Error retrieving reels for email={}, sectionType={}: {}", email, sectionType, throwable.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    @GetMapping(value = "/user-reels/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<List<MediaFileResponse>>> getReels(
            @PathVariable String email,
            @PathVariable String intent) {
        return mediaRetrievalService.getUserReels(email, intent)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    log.error("Error retrieving user reels for email={}: {}", email, throwable.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }
}
