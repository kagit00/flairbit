package com.dating.flairbit.repo;

import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.models.MatchSuggestionsImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MatchSuggestionsImportJobRepository extends JpaRepository<MatchSuggestionsImportJob, UUID> {
    @Modifying
    @Query("UPDATE MatchSuggestionsImportJob g SET g.status = :status WHERE g.id = :jobId")
    void updateStatus(@Param("jobId") UUID jobId, @Param("status") JobStatus status);

    @Modifying
    @Query("UPDATE MatchSuggestionsImportJob g SET g.totalRows = :total WHERE g.id = :jobId")
    void updateTotalRows(@Param("jobId") UUID jobId, @Param("total") int total);

    @Modifying
    @Query("UPDATE MatchSuggestionsImportJob g SET g.processedRows = g.processedRows + :count WHERE g.id = :jobId")
    void incrementProcessed(@Param("jobId") UUID jobId, @Param("count") int count);

    @Modifying
    @Query("UPDATE MatchSuggestionsImportJob g SET g.status = 'COMPLETED', g.completedAt = CURRENT_TIMESTAMP WHERE g.id = :jobId")
    void markCompleted(@Param("jobId") UUID jobId);

    @Modifying
    @Query("UPDATE MatchSuggestionsImportJob g SET g.status = 'FAILED', g.errorMessage = :error WHERE g.id = :jobId")
    void markFailed(@Param("jobId") UUID jobId, @Param("error") String error);

    @Query("SELECT g.processedRows FROM MatchSuggestionsImportJob g WHERE g.id = :jobId")
    int getProcessedRows(@Param("jobId") UUID jobId);

    @Query("SELECT g.totalRows FROM MatchSuggestionsImportJob g WHERE g.id = :jobId")
    int getTotalNodes(@Param("jobId") UUID jobId);
}
