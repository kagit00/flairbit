package com.dating.flairbit.repo;

import com.dating.flairbit.dto.enums.InteractionType;
import com.dating.flairbit.models.ReelInteraction;
import com.dating.flairbit.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReelInteractionRepository extends JpaRepository<ReelInteraction, UUID> {
    @Query("SELECT COUNT(ri) FROM ReelInteraction ri WHERE ri.user = :user AND ri.interactionType = :type")
    long countReactionsByUserAndType(@Param("user") User user, @Param("type") InteractionType type);
}
