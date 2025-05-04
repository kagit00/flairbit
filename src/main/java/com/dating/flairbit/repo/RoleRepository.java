package com.dating.flairbit.repo;

import com.dating.flairbit.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for performing CRUD operations on {@link Role} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide methods for saving,
 * retrieving, updating, and deleting {@link Role} entities from the database.
 * It uses {@link Long} as the identifier type for {@link Role} entities.
 * </p>
 *
 * Additionally, it provides a method to find a {@link Role} by its name.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Finds a {@link Role} entity by its name.
     *
     * @param name the name of the role to be found
     * @return the {@link Role} entity with the specified name, or null if not found
     */
    Role findByName(String name);
}

