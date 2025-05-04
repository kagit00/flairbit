package com.dating.flairbit.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "educations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education implements Serializable {

    private static final long serialVersionUID = -3456789012345678901L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    @JsonBackReference
    private Profile profile;

    @Column(name = "degree")
    @Size(max = 100)
    private String degree;

    @Column(name = "institution")
    @Size(max = 200)
    private String institution;

    @Column(name = "field_of_study")
    @Size(max = 100)
    private String fieldOfStudy;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
