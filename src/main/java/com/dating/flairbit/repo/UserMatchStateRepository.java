package com.dating.flairbit.repo;

import com.dating.flairbit.models.UserMatchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMatchStateRepository extends JpaRepository<UserMatchState, UUID> {

    @Modifying
    @Query("""
    UPDATE UserMatchState ums SET ums.sentToMatchingService = true WHERE ums.profile.user.id IN :userIds
    """)
    void markSentToMatchingService(@Param("userIds") List<UUID> userIds);

}
