package com.dating.flairbit.repo;

import com.dating.flairbit.models.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchSuggestionsRepository extends JpaRepository<MatchSuggestion, UUID> {
    List<MatchSuggestion> findByParticipantIdAndGroupId(String participantUsername, String groupId);
}