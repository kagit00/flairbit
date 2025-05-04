package com.dating.flairbit.repo;

import com.dating.flairbit.models.User;
import org.springframework.cache.annotation.Cacheable;
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

    @Query("""
    SELECT DISTINCT u FROM User u JOIN u.profiles p JOIN p.userMatchState ms
    WHERE ms.groupId = :groupId AND ms.sentToMatchingService = false AND ms.profileComplete = true AND ms.readyForMatching = true
    """)
    List<User> findByGroupIdAndSentToMatchingServiceFalse(@Param("groupId") String groupId);


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    User findByUsernameWithRoles(String username);

    @Query("SELECT u.id FROM User u WHERE u.username IN :usernames")
    List<UUID> findIdsByUsernames(@Param("usernames") List<String> usernames);

    @Query("SELECT u.email FROM User u")
    List<String> findAllEmails();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profiles WHERE u.email = :email")
    Optional<User> findByEmailFetchProfiles(@Param("email") String email);

}
