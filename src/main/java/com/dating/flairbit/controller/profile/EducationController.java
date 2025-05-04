package com.dating.flairbit.controller.profile;

import com.dating.flairbit.dto.EducationRequest;
import com.dating.flairbit.dto.EducationResponse;
import com.dating.flairbit.service.profile.education.EducationRetrievalService;
import com.dating.flairbit.service.profile.education.EducationUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{email}/education")
@AllArgsConstructor
@Tag(name = "Education API", description = "Manage user's education details")
public class EducationController {

    private final EducationUpdateService educationUpdateService;
    private final EducationRetrievalService educationRetrievalService;

    @Operation(summary = "Get user's education info")
    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EducationResponse> getEducation(@PathVariable String email, @PathVariable String intent) {
        return ResponseEntity.ok(educationRetrievalService.getEducation(email, intent));
    }

    @Operation(summary = "Update user's education info")
    @PutMapping(
            value = "/{intent}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public ResponseEntity<Void> createOrUpdateEducation(@PathVariable String email,
                                                @RequestBody @Valid EducationRequest educationRequest, @PathVariable String intent) {
        educationUpdateService.createOrUpdateEducation(email, educationRequest, intent);
        return ResponseEntity.ok().build();
    }
}
