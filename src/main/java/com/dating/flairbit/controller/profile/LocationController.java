package com.dating.flairbit.controller.profile;

import com.dating.flairbit.dto.LocationRequest;
import com.dating.flairbit.dto.LocationResponse;
import com.dating.flairbit.service.profile.location.LocationRetrievalService;
import com.dating.flairbit.service.profile.location.LocationUpdateService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/users/{email}/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationUpdateService locationUpdateService;
    private final LocationRetrievalService locationRetrievalService;

    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocationResponse> getLocation(@PathVariable String email, @PathVariable String intent) {
        LocationResponse response = locationRetrievalService.getLocation(email, intent);
        return ResponseEntity.ok(response);
    }

    @PutMapping(
            value = "/{intent}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public ResponseEntity<Void> createOrUpdateLocation(@PathVariable String email, @RequestBody @Valid LocationRequest request, @PathVariable String intent) {
        locationUpdateService.createOrUpdateLocation(email, request, intent);
        return ResponseEntity.ok().build();
    }
}
