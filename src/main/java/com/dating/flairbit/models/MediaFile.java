package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.ReelType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "media_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile implements Serializable {

    private static final long serialVersionUID = -4567891234567891234L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonBackReference
    private Profile profile;

    @Column(name = "original_file_name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String originalFileName;

    @Column(name = "file_type", nullable = false)
    @NotBlank
    @Size(max = 50)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reel_type")
    private ReelType reelType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    @NotBlank
    @Size(max = 500)
    private String filePath;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Version
    private Long version;
}