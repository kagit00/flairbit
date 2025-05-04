package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.MatchRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "match_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_request_from_to_reel", columnNames = {"from_user_id", "to_user_id", "reel_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest implements Serializable {

    private static final long serialVersionUID = -6789013456789013456L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    @NotNull
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    @NotNull
    private User toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = false)
    @NotNull
    private MediaFile reel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private MatchRequestStatus status = MatchRequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}