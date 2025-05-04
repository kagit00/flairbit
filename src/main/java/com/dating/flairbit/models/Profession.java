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
@Table(name = "professions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profession implements Serializable {

    private static final long serialVersionUID = -4567890123456789012L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonBackReference
    private Profile profile;

    @Column(name = "job_title")
    @Size(max = 100)
    private String jobTitle;

    @Column(name = "company")
    @Size(max = 200)
    private String company;

    @Column(name = "industry")
    @Size(max = 100)
    private String industry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
