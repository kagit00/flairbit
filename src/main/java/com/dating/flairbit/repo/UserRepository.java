package com.dating.flairbit.repo;

import com.dating.flairbit.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query(value = """
    SELECT 
        u.id AS userId, u.username AS username, p.id AS profileId, p.display_name AS displayName, 
        p.bio AS bio, l.city AS city, ls.smokes AS smokes, ls.drinks AS drinks,
        ls.religion AS religion, pr.wants_kids AS wantsKids, 
        pr.preferred_genders AS preferredGenders,
        pr.preferred_min_age AS preferredMinAge, pr.preferred_max_age AS preferredMaxAge,
        pr.relationship_type AS relationshipType, pr.open_to_long_distance AS openToLongDistance,
        e.field_of_study AS fieldOfStudy, pro.industry AS industry,
        ms.gender AS gender, ms.date_of_birth AS dateOfBirth,
        ms.intent AS intent, ms.ready_for_matching AS readyForMatching, ms.group_id AS groupId
    FROM users u 
    JOIN profiles p ON u.id = p.user_id
    JOIN user_match_states ms ON ms.profile_id = p.id
    LEFT JOIN locations l ON l.profile_id = p.id
    LEFT JOIN lifestyles ls ON ls.profile_id = p.id
    LEFT JOIN preferences pr ON pr.profile_id = p.id
    LEFT JOIN educations e ON e.profile_id = p.id
    LEFT JOIN professions pro ON pro.profile_id = p.id
    WHERE ms.group_id = :groupId AND ms.sent_to_matching_service = false
    """,
            countQuery = """
        SELECT COUNT(*)
        FROM user_match_states ms
        JOIN profiles p ON ms.profile_id = p.id
        WHERE ms.group_id = :groupId AND ms.sent_to_matching_service = false
    """,
            nativeQuery = true
    )
    Page<Object[]> findByGroupIdAndSentToMatchingServiceFalse(@Param("groupId") String groupId, Pageable pageable);


    @Query("SELECT u.id FROM User u WHERE u.username IN :usernames")
    List<UUID> findIdsByUsernames(@Param("usernames") List<String> usernames);

}
