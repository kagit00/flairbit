package com.dating.flairbit.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "user_engagement_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEngagementStats implements Serializable {

    private static final long serialVersionUID = -1234567890123456789L;

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Column(name = "super_likes_sent", nullable = false)
    private int superLikesSent = 0;

    @Column(name = "match_requests_sent", nullable = false)
    private int matchRequestsSent = 0;

    @Column(name = "match_requests_received", nullable = false)
    private int matchRequestsReceived = 0;

    @Column(name = "matches_count", nullable = false)
    private int matchesCount = 0;

    @Version
    private Long version;
}