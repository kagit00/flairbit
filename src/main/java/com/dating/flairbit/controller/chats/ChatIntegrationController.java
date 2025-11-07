package com.dating.flairbit.controller.chats;

import com.dating.flairbit.dto.ProfileChatDto;
import com.dating.flairbit.dto.ProfileResponse;
import com.dating.flairbit.service.profile.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/chat-service")
@AllArgsConstructor
public class ChatIntegrationController {
    private final ProfileService profileService;

    @GetMapping(value = "/users/{email}/profile/{intent}")
    @PreAuthorize("hasAuthority('INTERNAL_SERVICE')")
    public ResponseEntity<ProfileChatDto> getProfileForChat(@PathVariable String email, @PathVariable String intent) {
        ProfileResponse profileResponse = profileService.getProfile(email, intent);
        ProfileChatDto profileChatDto = ProfileChatDto.builder()
                .id(profileResponse.getId()).displayName(profileResponse.getDisplayName())
                .build();
        return new ResponseEntity<>(profileChatDto, HttpStatusCode.valueOf(200));
    }
}
