package com.dating.flairbit.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Data
@Entity
@Table(name = "match_suggestions")
public class MatchSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String groupId;
    @Column(nullable = false)
    private String participantId;
    @Column(nullable = false)
    private String matchedParticipantId;
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Double compatibilityScore;
}
