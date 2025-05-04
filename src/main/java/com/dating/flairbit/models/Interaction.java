package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.InteractionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interactions", indexes = {
    @Index(name = "idx_interactions_from_to", columnList = "from_user_id,to_user_id"),
    @Index(name = "idx_interactions_type", columnList = "interaction_type")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;

    private LocalDateTime createdAt;
}
