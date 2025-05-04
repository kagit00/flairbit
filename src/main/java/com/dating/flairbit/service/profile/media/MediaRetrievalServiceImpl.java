package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.*;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.MediaFileRepository;
import com.dating.flairbit.service.GroupConfigService;
import com.dating.flairbit.service.match.suggestions.MatchSuggestionsStorageService;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class MediaRetrievalServiceImpl implements MediaRetrievalService {
    private final MediaFileRepository mediaFileRepository;
    private final UserService userService;
    private final MatchSuggestionsStorageService matchSuggestionsStorageService;
    private final GroupConfigService groupConfigService;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional(readOnly = true)
    public List<MediaFileResponse> getUserReels(String email, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        List<MediaFile> mediaFiles = mediaFileRepository.findByProfile(profile);
        return mediaFiles.stream()
                .map(ResponseMakerUtility::getMediaFileResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "mediaCache", key = "#id", unless = "#result == null")
    public MediaFile getMediaFileById(UUID id) {
        return mediaFileRepository.findById(id).orElseThrow(
                () -> new BadRequestException("Media not found")
        );
    }

    @Override
    @Cacheable(value = "mediaCache", key = "#participantUsername + '_matched_user_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent")
    public List<MediaFileResponse> getMatchedUsersReels(String participantUsername, LocalDateTime cursor, int limit, String intent) {
        MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
        List<String> matchedUsernames = matchSuggestionsStorageService.retrieveByParticipantIdAndGroupId(participantUsername, group.getId()).stream()
                .map(MatchSuggestion::getMatchedParticipantId)
                .distinct()
                .toList();

        List<MediaFile> reels = new ArrayList<>();
        int remaining = limit;
        for (List<String> batch : Lists.partition(matchedUsernames, 100)) {
            List<MediaFile> batchResult = mediaFileRepository.findMediaFilesByUsernames(batch, cursor, limit);
            reels.addAll(batchResult);
            remaining -= batchResult.size();
            if (remaining <= 0) break;
        }

        return reels.stream()
                .map(ResponseMakerUtility::getMediaFileResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "mediaCache", key = "'most_liked_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent")
    public List<MediaFileResponse> getMostLikedReels(LocalDateTime cursor, int limit, String intent) {
        MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
        List<MediaFile> reels = mediaFileRepository.findMostLikedByGroupId(group.getId(), cursor, limit);

        return reels.stream()
                .map(ResponseMakerUtility::getMediaFileResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "mediaCache", key = "'most_viewed_reels_' + #cursor?.toString() + '_' + #limit + '_' + #intent")
    public List<MediaFileResponse> getMostViewedReels(LocalDateTime cursor, int limit, String intent) {
        MatchingGroupConfig group = groupConfigService.getGroupConfig(intent);
        List<MediaFile> reels = mediaFileRepository.findMostViewedByGroupId(group.getId(), cursor, limit);

        return reels.stream()
                .map(ResponseMakerUtility::getMediaFileResponse)
                .toList();
    }
}
