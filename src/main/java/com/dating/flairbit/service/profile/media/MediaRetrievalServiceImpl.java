package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.*;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.MediaFileRepository;
import com.dating.flairbit.service.GroupConfigService;
import com.dating.flairbit.service.match.suggestions.MatchSuggestionsStorageService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.dating.flairbit.utils.db.BatchUtils.partition;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaRetrievalServiceImpl implements MediaRetrievalService {

    private final MediaFileRepository mediaFileRepository;
    private final UserService userService;
    private final MatchSuggestionsStorageService matchSuggestionsStorageService;
    private final GroupConfigService groupConfigService;
    private final ProfileProcessor profileProcessor;
    private static final int BATCH_SIZE = 100;

    @Override
    @Cacheable(value = "mediaCache", key = "#email + '_' + #intent", unless = "#result == null")
    @Transactional(readOnly = true)
    public CompletableFuture<List<MediaFileResponse>> getUserReels(String email, String intent) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userService.getUserByEmail(email);
            Profile profile = profileProcessor.getProfile(user, intent);
            List<MediaFile> mediaFiles = mediaFileRepository.findByProfile(profile);
            return mediaFiles.stream()
                    .map(ResponseMakerUtility::getMediaFileResponse)
                    .toList();
        }).exceptionally(throwable -> {
            log.error("Failed to retrieve user reels for email={} and intent={}: {}", email, intent, throwable.getMessage());
            throw new CompletionException("Failed to retrieve user reels", throwable);
        });
    }

    @Override
    @Cacheable(value = "mediaCache", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public CompletableFuture<MediaFile> getMediaFileById(UUID id) {
        return CompletableFuture.supplyAsync(() ->
                mediaFileRepository.findById(id)
                        .orElseThrow(() -> new BadRequestException("Media not found"))
        ).exceptionally(throwable -> {
            log.error("Failed to retrieve media file for id={}: {}", id, throwable.getMessage());
            throw new CompletionException("Failed to retrieve media file", throwable);
        });
    }

    @Override
    @Cacheable(
            value = "mediaCache",
            key = "#participantUsername + '_potential_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public CompletableFuture<List<MediaFileResponse>> getMatchedUsersReels(String participantUsername, LocalDateTime cursor, int limit, String intent) {
        return CompletableFuture.supplyAsync(() -> {
            MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
            return matchSuggestionsStorageService.retrieveMatchSuggestions(participantUsername, group.getId());
        }).thenCompose(suggestionsFuture ->
                suggestionsFuture.thenApply(suggestions -> suggestions.stream()
                        .map(MatchSuggestion::getMatchedParticipantId)
                        .distinct()
                        .toList())
        ).thenCompose(suggestedUsernames -> {
            List<CompletableFuture<List<MediaFile>>> batchFutures = new ArrayList<>();
            int maxLimit = limit;

            for (List<String> batch : partition(suggestedUsernames, BATCH_SIZE)) {
                if (maxLimit <= 0) break;
                int batchLimit = Math.min(maxLimit, BATCH_SIZE);
                batchFutures.add(CompletableFuture.supplyAsync(() ->
                        mediaFileRepository.findMediaFilesByUsernames(batch, cursor, batchLimit)
                ));
                maxLimit -= batchLimit;
            }

            return CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> batchFutures.stream()
                            .flatMap(future -> future.join().stream())
                            .limit(limit)
                            .map(ResponseMakerUtility::getMediaFileResponse)
                            .toList());
        }).exceptionally(throwable -> {
            log.error("Failed to retrieve matched users' reels for participantUsername={}: {}", participantUsername, throwable.getMessage());
            throw new CompletionException("Failed to retrieve matched reels", throwable);
        });
    }

    @Override
    @Cacheable(
            value = "mediaCache",
            key = "'most_liked_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public CompletableFuture<List<MediaFileResponse>> getMostLikedReels(LocalDateTime cursor, int limit, String intent) {
        return CompletableFuture.supplyAsync(() -> {
            MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
            List<MediaFile> reels = mediaFileRepository.findMostLikedByGroupId(group.getId(), cursor, limit);
            return reels.stream()
                    .map(ResponseMakerUtility::getMediaFileResponse)
                    .toList();
        }).exceptionally(throwable -> {
            log.error("Failed to retrieve most liked reels for intent={}: {}", intent, throwable.getMessage());
            throw new CompletionException("Failed to retrieve most liked reels", throwable);
        });
    }

    @Override
    @Cacheable(
            value = "mediaCache",
            key = "'most_viewed_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public CompletableFuture<List<MediaFileResponse>> getMostViewedReels(LocalDateTime cursor, int limit, String intent) {
        return CompletableFuture.supplyAsync(() -> {
            MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
            List<MediaFile> reels = mediaFileRepository.findMostViewedByGroupId(group.getId(), cursor, limit);
            return reels.stream()
                    .map(ResponseMakerUtility::getMediaFileResponse)
                    .toList();
        }).exceptionally(throwable -> {
            log.error("Failed to retrieve most viewed reels for intent={}: {}", intent, throwable.getMessage());
            throw new CompletionException("Failed to retrieve most viewed reels", throwable);
        });
    }
}