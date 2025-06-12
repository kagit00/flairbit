package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.models.MediaFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface MediaRetrievalService {
    CompletableFuture<List<MediaFileResponse>> getUserReels(String email, String intent);
    CompletableFuture<MediaFile> getMediaFileById(UUID id);
    CompletableFuture<List<MediaFileResponse>> getMostLikedReels(LocalDateTime cursor, int limit, String intent);
    CompletableFuture<List<MediaFileResponse>> getMatchedUsersReels(String participantUsername, LocalDateTime cursor, int limit, String intent);
    CompletableFuture<List<MediaFileResponse>> getMostViewedReels(LocalDateTime cursor, int limit, String intent);
}