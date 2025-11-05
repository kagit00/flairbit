package com.dating.flairbit.service.matchrequests;

import com.dating.flairbit.dto.enums.MatchRequestStatus;
import com.dating.flairbit.models.MatchRequest;

import java.util.List;
import java.util.UUID;

public interface MatchRequestService {
    void send(String from, String to, String intent);
    void respond(String from, String to, String intent, MatchRequestStatus newStatus);
    List<MatchRequest> getSentRequests(String email, String intent);
    List<MatchRequest> getReceivedRequests(String email, String intent);
}
