package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaFileResponse;
import com.dating.flairbit.models.MediaFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface MediaRetrievalService {
    List<MediaFileResponse> getUserReels(String email, String intent);
    MediaFile getMediaFileById(UUID id);
    List<MediaFileResponse> getMostLikedReels(LocalDateTime cursor, int limit, String intent);
    List<MediaFileResponse> getMatchedUsersReels(String participantUsername, LocalDateTime cursor, int limit, String intent);
    List<MediaFileResponse> getMostViewedReels(LocalDateTime cursor, int limit, String intent);
}
