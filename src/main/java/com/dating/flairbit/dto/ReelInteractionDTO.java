package com.dating.flairbit.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReelInteractionDTO {

    @NotNull
    private String userEmail;

    @NotNull
    private UUID reelId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private String interactionType;
}