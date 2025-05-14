package com.dating.flairbit.repo;

import com.dating.flairbit.dto.enums.ReelType;
import com.dating.flairbit.models.MediaFile;
import com.dating.flairbit.models.Profile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    Optional<MediaFile> findByProfileAndReelType(Profile profile, ReelType reelType);
    int countByProfile(Profile profile);

    List<MediaFile> findByProfile(Profile profile);

    @Query(value = """
    SELECT mf.* FROM media_files mf  JOIN media_view_counts mvc ON mf.id = mvc.reel_id  JOIN profiles p ON mf.profile_id = p.id
    JOIN user_match_state ums ON p.id = ums.profile_id WHERE ums.group_id = :groupId AND mf.uploaded_at < :cursor
    ORDER BY mvc.view_count DESC, mf.uploaded_at DESC LIMIT :limit
    """, nativeQuery = true)
    List<MediaFile> findMostViewedByGroupId(
            @Param("groupId") String groupId,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit
    );


    @Query(value = """
    SELECT mf.* FROM media_files mf JOIN media_like_counts mlc ON mf.id = mlc.reel_id JOIN profiles p ON mf.profile_id = p.id
    JOIN user_match_state ums ON p.id = ums.profile_id WHERE ums.group_id = :groupId AND mf.uploaded_at < :cursor
    ORDER BY mlc.like_count DESC, mf.uploaded_at DESC LIMIT :limit
    """, nativeQuery = true)
    List<MediaFile> findMostLikedByGroupId(
            @Param("groupId") String groupId,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit
    );


    @Query(value = """
    SELECT mf.* FROM media_files mf JOIN profiles p ON mf.profile_id = p.id JOIN users u ON p.user_id = u.id
    WHERE u.username IN (:usernames) AND mf.uploaded_at < :cursor ORDER BY mf.uploaded_at DESC LIMIT :limit
    """, nativeQuery = true)
    List<MediaFile> findMediaFilesByUsernames(
            @Param("usernames") List<String> usernames,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit);
}