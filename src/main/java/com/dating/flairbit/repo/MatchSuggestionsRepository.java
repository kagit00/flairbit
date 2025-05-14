package com.dating.flairbit.repo;

import com.dating.flairbit.models.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchSuggestionsRepository extends JpaRepository<MatchSuggestion, UUID> {

    @Query(value = """
    SELECT ms.*
    FROM match_suggestions ms
    JOIN users matched_u ON matched_u.username = ms.matched_participant_id
    JOIN profiles matched_p ON matched_p.user_id = matched_u.id
    JOIN user_match_states matched_ums ON matched_ums.profile_id = matched_p.id
        AND matched_ums.group_id = :groupId
    JOIN users participant_u ON participant_u.username = ms.participant_id
    JOIN profiles participant_p ON participant_p.user_id = participant_u.id
    JOIN preferences pref ON pref.profile_id = participant_p.id
    JOIN user_match_states participant_ums ON participant_ums.profile_id = participant_p.id
        AND participant_ums.group_id = ms.group_id
    WHERE ms.participant_id = :participantUsername
      AND ms.group_id = :groupId
      AND matched_ums.gender = ANY (
          string_to_array(trim(both '{}' from pref.preferred_genders), ',')
      )
    ORDER BY ms.compatibility_score DESC
    """, nativeQuery = true)
    List<MatchSuggestion> findFilteredSuggestions(
            @Param("participantUsername") String participantUsername,
            @Param("groupId") String groupId
    );

}