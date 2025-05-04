package com.dating.flairbit.controller.profile;

import com.dating.flairbit.dto.PreferencesRequest;
import com.dating.flairbit.dto.PreferencesResponse;
import com.dating.flairbit.service.profile.prefrerences.PreferencesRetrievalService;
import com.dating.flairbit.service.profile.prefrerences.PreferencesUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{email}/preferences")
@AllArgsConstructor
@Tag(name = "Preferences API", description = "Manage user's preferences for matching")
public class PreferencesController {

    private final PreferencesUpdateService preferencesUpdateService;
    private final PreferencesRetrievalService preferencesRetrievalService;

    @Operation(summary = "Get user's preferences")
    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PreferencesResponse> getPreferences(@PathVariable String email, @PathVariable String intent) {
        return ResponseEntity.ok(preferencesRetrievalService.getPreferences(email, intent));
    }

    @Operation(summary = "Update user's preferences")
    @PutMapping(value = "/{intent}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrUpdatePreferences(@PathVariable String email,
                                                          @RequestBody @Valid PreferencesRequest preferencesRequest, @PathVariable String intent) {
        preferencesUpdateService.createOrUpdatePreferences(email, preferencesRequest, intent);
        return ResponseEntity.ok().build();
    }
}
