package com.dating.flairbit.repo;

import com.dating.flairbit.models.Profile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserIdAndUserMatchState_Intent(UUID userId, String intent);
}
