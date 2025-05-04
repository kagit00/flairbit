package com.dating.flairbit.service.interaction;

import java.util.UUID;

public interface InteractionService {
    void likeUser(UUID fromUserId, UUID toUserId);
    void dislikeUser(UUID fromUserId, UUID toUserId);
    void superLikeUser(UUID fromUserId, UUID toUserId);
}