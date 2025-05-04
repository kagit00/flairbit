package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.GenderType;
import com.dating.flairbit.dto.enums.IntentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "user_match_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMatchState implements Serializable {

    private static final long serialVersionUID = -98765432187654321L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    @JsonBackReference
    private Profile profile;

    @Column(name = "sent_to_matching_service", nullable = false)
    private boolean sentToMatchingService = false;

    @Column(name = "profile_complete", nullable = false)
    private boolean profileComplete = false;

    @Column(name = "ready_for_matching", nullable = false)
    private boolean readyForMatching = false;

    @Column(name = "intent", nullable = false)
    private String intent;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "last_matched_at")
    private LocalDateTime lastMatchedAt;

    @Column(name = "group_id")
    private String groupId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}