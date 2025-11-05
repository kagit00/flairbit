package com.dating.flairbit.repo;

import com.dating.flairbit.models.MatchRequest;
import com.dating.flairbit.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRequestRepository extends JpaRepository<MatchRequest, UUID> {
    List<MatchRequest> findByFrom(Profile from);
    List<MatchRequest> findByTo(Profile to);
    Optional<MatchRequest> findByFromAndTo(Profile from, Profile to);
}
