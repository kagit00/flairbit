package com.dating.flairbit.repo;

import com.dating.flairbit.models.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for performing CRUD operations on {@link Audit} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide methods for saving,
 * retrieving, updating, and deleting {@link Audit} entities from the database.
 * It uses UUID as the identifier type for {@link Audit} entities.
 * </p>
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, UUID> {
}
