package com.dating.flairbit.controller.profile;

import com.dating.flairbit.dto.ProfessionRequest;
import com.dating.flairbit.dto.ProfessionResponse;
import com.dating.flairbit.service.profile.profession.ProfessionRetrievalService;
import com.dating.flairbit.service.profile.profession.ProfessionUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{email}/profession")
@AllArgsConstructor
@Tag(name = "Profession API", description = "Manage user's professional details")
public class ProfessionController {

    private final ProfessionUpdateService professionUpdateService;
    private final ProfessionRetrievalService professionRetrievalService;

    @Operation(summary = "Get user's profession info")
    @GetMapping(value = "/{intent}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfessionResponse> getProfession(@PathVariable String email, @PathVariable String intent) {
        return ResponseEntity.ok(professionRetrievalService.getProfession(email, intent));
    }

    @Transactional
    @Operation(summary = "Update user's profession info")
    @PutMapping(value = "/{intent}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrUpdateProfession(@PathVariable String email,
                                                 @RequestBody @Valid ProfessionRequest professionRequest, @PathVariable String intent) {
        professionUpdateService.createOrUpdateProfession(email, professionRequest, intent);
        return ResponseEntity.ok().build();
    }
}
