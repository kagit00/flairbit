package com.dating.flairbit.models;


import com.dating.flairbit.dto.enums.GenderType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Size;
import java.io.Serializable;


@Entity
@Table(name = "preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preferences implements Serializable {

    private static final long serialVersionUID = -7890123456789012345L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonBackReference
    private Profile profile;

    private Set<String> preferredGenders = new HashSet<>();

    @Column(name = "preferred_min_age")
    private Integer preferredMinAge;

    @Column(name = "preferred_max_age")
    private Integer preferredMaxAge;

    @Column(name = "relationship_type")
    @Size(max = 50)
    private String relationshipType;

    @Column(name = "wants_kids")
    private Boolean wantsKids;

    @Column(name = "open_to_long_distance")
    private Boolean openToLongDistance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}