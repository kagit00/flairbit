package com.dating.flairbit.models;

import com.dating.flairbit.dto.enums.JobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "match_suggestions_import_jobs")
public class MatchSuggestionsImportJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id")
    private String groupId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "total_rows")
    private int totalRows;

    @Column(name = "processed_rows")
    private int processedRows;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
