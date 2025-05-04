package com.dating.flairbit.service.matchrequests;

import com.dating.flairbit.dto.enums.MatchRequestStatus;
import com.dating.flairbit.models.MatchRequest;

import java.util.List;
import java.util.UUID;

public interface MatchRequestService {
    MatchRequest send(UUID fromUserId, UUID toUserId, UUID reelId);
    void respond(UUID requestId, MatchRequestStatus newStatus);
    List<MatchRequest> getSentRequests(UUID userId);
    List<MatchRequest> getReceivedRequests(UUID userId);
}
