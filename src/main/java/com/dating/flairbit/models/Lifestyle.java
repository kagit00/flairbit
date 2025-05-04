package com.dating.flairbit.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.Size;

import java.io.Serializable;


@Entity
@Table(name = "lifestyles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lifestyle implements Serializable {

    private static final long serialVersionUID = -6789012345678901234L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonBackReference
    private Profile profile;

    @Column(name = "drinks")
    private Boolean drinks;

    @Column(name = "smokes")
    private Boolean smokes;

    @Column(name = "religion")
    @Size(max = 50)
    private String religion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}