package com.dating.flairbit.controller.profile;


import com.dating.flairbit.dto.ProfileRequest;
import com.dating.flairbit.dto.ProfileResponse;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.service.profile.ProfileService;
import com.dating.flairbit.utils.response.ResponseMakerUtility;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{email}/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Transactional
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponse> createOrUpdateProfile(@PathVariable String email, @RequestBody @Valid ProfileRequest profileRequest) {
        Profile profile = profileService.createOrUpdateProfile(email, profileRequest);
        return ResponseEntity.ok(ResponseMakerUtility.getProfileResponse(profile));
    }


    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String email, @PathVariable String intent) {
        ProfileResponse profile = profileService.getProfile(email, intent);
        return ResponseEntity.ok(profile);
    }
}

