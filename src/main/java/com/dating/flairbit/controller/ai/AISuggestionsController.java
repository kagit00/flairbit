package com.dating.flairbit.controller.ai;

import com.dating.flairbit.models.AISuggestion;
import com.dating.flairbit.service.ai.AISuggestionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/mindfulness")
@AllArgsConstructor
@Tag(name = "Resume Related AI Suggestions API", description = "Operations related to AI suggestions for resume")
public class AISuggestionsController {

    private final AISuggestionsService aiSuggestionsService;


    @Operation(summary = "Generate Suggestions for Resume Summary",
            description = "Requires either JWT or OAuth2 for authentication",
            security = {
                    @SecurityRequirement(name = "JWT"),
                    @SecurityRequirement(name = "OAuth2")
            })
    @PostMapping("/ai/suggestions")
    @Transactional
    public ResponseEntity<AISuggestion> generateSummary(@RequestParam String title, @RequestParam String sectionType) {
        AISuggestion aiSuggestion = aiSuggestionsService.generateSuggestions(title, sectionType);
        return new ResponseEntity<>(aiSuggestion, HttpStatus.OK);
    }
}

