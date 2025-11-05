package com.dating.flairbit.controller.matchrequests;

import com.dating.flairbit.dto.enums.MatchRequestStatus;
import com.dating.flairbit.models.MatchRequest;
import com.dating.flairbit.service.matchrequests.MatchRequestService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/users/{from}")
@RequiredArgsConstructor
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PutMapping(
            value = "/match-request/{to}/{intent}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> sendRequest(
            @PathVariable @Email String from,
            @PathVariable @Email String to,
            @PathVariable String intent) {
            matchRequestService.send(from, to, intent);
            return ResponseEntity.ok().build();
    }

    @PutMapping(
            value = "/match-request/{to}/{intent}/{newStatus}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> respondToRequest(
            @PathVariable @Email String from,
            @PathVariable @Email String to,
            @PathVariable String intent,
            @PathVariable MatchRequestStatus newStatus) {
            matchRequestService.respond(from, to, intent, newStatus);
            return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/match-request/sent/{intent}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MatchRequest>> getSent(
            @PathVariable @Email String from,
            @PathVariable String intent
    ) {
        List<MatchRequest> list = matchRequestService.getSentRequests(from, intent);
        return ResponseEntity.ok(list);
    }


    @GetMapping(value = "/match-request/received/{intent}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MatchRequest>> getReceived(
            @PathVariable @Email String from,
            @PathVariable String intent
    ) {
        List<MatchRequest> list = matchRequestService.getReceivedRequests(from, intent);
        return ResponseEntity.ok(list);
    }
}
