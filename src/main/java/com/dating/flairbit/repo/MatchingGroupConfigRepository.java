package com.dating.flairbit.repo;

import com.dating.flairbit.models.MatchingGroupConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MatchingGroupConfigRepository extends JpaRepository<MatchingGroupConfig, String> {
    Optional<MatchingGroupConfig> findByIntentAndActiveTrue(String intent);
}
