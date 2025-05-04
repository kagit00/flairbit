package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.InteractionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "reel_interactions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reel_interaction_user_reel_type", columnNames = {"user_id", "reel_id", "interaction_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReelInteraction implements Serializable {

    private static final long serialVersionUID = -5678902345678902345L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = false)
    @NotNull
    private MediaFile reel;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    @NotNull
    private InteractionType interactionType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}