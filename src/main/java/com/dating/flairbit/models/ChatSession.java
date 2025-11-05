package com.dating.flairbit.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_participants_intent", columnList = "profile1_id,profile2_id,intent")
}, uniqueConstraints = {@UniqueConstraint(columnNames = {"profile1_id","profile2_id","intent"})})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile1_id", nullable = false)
    private Profile profile1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile2_id", nullable = false)
    private Profile profile2;

    @Column(name = "intent", nullable = false)
    private String intent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}