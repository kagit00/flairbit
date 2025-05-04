package com.dating.flairbit.controller.profile;

import com.dating.flairbit.dto.LifestyleRequest;
import com.dating.flairbit.dto.LifestyleResponse;
import com.dating.flairbit.service.profile.lifestyle.LifeStyleRetrievalService;
import com.dating.flairbit.service.profile.lifestyle.LifeStyleUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{email}/lifestyle")
@AllArgsConstructor
@Tag(name = "Lifestyle API", description = "Manage user's lifestyle choices like drinking, smoking, religion")
public class LifestyleController {

    private final LifeStyleUpdateService lifeStyleUpdateService;
    private final LifeStyleRetrievalService lifeStyleRetrievalService;

    @Operation(summary = "Get user's lifestyle info")
    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<LifestyleResponse> getLifestyle(@PathVariable String email, @PathVariable String intent) {
        return ResponseEntity.ok(lifeStyleRetrievalService.getLifestyle(email, intent));
    }

    @Operation(summary = "Update user's lifestyle info")
    @PutMapping(
            value = "/{intent}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public ResponseEntity<Void> createOrUpdateLifestyle(@PathVariable String email, @RequestBody @Valid LifestyleRequest lifestyleRequest, @PathVariable String intent) {
        lifeStyleUpdateService.createOrUpdateLifestyle(email, lifestyleRequest, intent);
        return ResponseEntity.ok().build();
    }
}
