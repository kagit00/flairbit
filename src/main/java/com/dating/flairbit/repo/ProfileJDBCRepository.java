package com.dating.flairbit.repo;

import com.dating.flairbit.dto.enums.ReelType;
import com.dating.flairbit.models.*;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class ProfileJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfileJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SQL_FIND_PROFILE_WITH_DETAILS =
            "SELECT " +
                    "p.id as p_id, p.user_id as p_user_id, p.display_name as p_display_name, p.headline as p_headline, " +
                    "p.bio as p_bio, p.created_at as p_created_at, p.updated_at as p_updated_at, p.version as p_version, " +

                    "e.id as e_id, e.degree as e_degree, e.institution as e_institution, e.field_of_study as e_field_of_study, " +
                    "e.graduation_year as e_graduation_year, e.created_at as e_created_at, e.updated_at as e_updated_at, e.version as e_version, " +

                    "pr.id as pr_id, pr.job_title as pr_job_title, pr.company as pr_company, pr.industry as pr_industry, " +
                    "pr.created_at as pr_created_at, pr.updated_at as pr_updated_at, pr.version as pr_version, " +

                    "l.id as l_id, l.city as l_city, l.country as l_country, l.latitude as l_latitude, l.longitude as l_longitude, " +
                    "l.created_at as l_created_at, l.updated_at as l_updated_at, l.version as l_version, " +

                    "ls.id as ls_id, ls.drinks as ls_drinks, ls.smokes as ls_smokes, ls.religion as ls_religion, " +
                    "ls.created_at as ls_created_at, ls.updated_at as ls_updated_at, ls.version as ls_version, " +

                    "pref.id as pref_id, pref.preferred_genders as pref_preferred_genders, pref.preferred_min_age as pref_preferred_min_age, pref.preferred_max_age as pref_preferred_max_age, " + // <-- ADDED HERE
                    "pref.relationship_type as pref_relationship_type, pref.wants_kids as pref_wants_kids, " +
                    "pref.open_to_long_distance as pref_open_to_long_distance, pref.created_at as pref_created_at, " +
                    "pref.updated_at as pref_updated_at, pref.version as pref_version, " +

                    "ums.id as ums_id, ums.sent_to_matching_service as ums_sent_to_matching_service, ums.profile_complete as ums_profile_complete, " +
                    "ums.ready_for_matching as ums_ready_for_matching, ums.intent as ums_intent, ums.gender as ums_gender, " +
                    "ums.date_of_birth as ums_date_of_birth, ums.last_matched_at as ums_last_matched_at, ums.group_id as ums_group_id, " +
                    "ums.created_at as ums_created_at, ums.updated_at as ums_updated_at, ums.version as ums_version " +

                    "FROM profiles p " +
                    "LEFT JOIN educations e ON p.id = e.profile_id " +
                    "LEFT JOIN professions pr ON p.id = pr.profile_id " +
                    "LEFT JOIN locations l ON p.id = l.profile_id " +
                    "LEFT JOIN lifestyles ls ON p.id = ls.profile_id " +
                    "LEFT JOIN preferences pref ON p.id = pref.profile_id " +
                    "LEFT JOIN user_match_states ums ON p.id = ums.profile_id " +
                    "WHERE p.user_id = ? AND ums.intent = ?";

    private static final String SQL_FIND_MEDIA_FILES_BY_PROFILE_ID =
            "SELECT id, original_file_name, file_type, reel_type, file_size, file_path, display_order, uploaded_at, version " +
                    "FROM media_files " +
                    "WHERE profile_id = ? " +
                    "ORDER BY display_order";


    public Profile findByUserIdAndIntent(UUID userId, String intent) {
        Profile profile;
        try {
            profile = this.jdbcTemplate.queryForObject(
                    SQL_FIND_PROFILE_WITH_DETAILS,
                    new ProfileWithDetailsRowMapper(),
                    userId, intent
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }

        if (profile != null) {
            List<MediaFile> mediaFiles = this.jdbcTemplate.query(
                    SQL_FIND_MEDIA_FILES_BY_PROFILE_ID,
                    new MediaFileRowMapper(),
                    profile.getId()
            );
            profile.setMediaFiles(mediaFiles);
        }

        return profile;
    }

    private static final class ProfileWithDetailsRowMapper implements RowMapper<Profile> {
        @Override
        public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
            Profile profile = new Profile();
            profile.setId(rs.getObject("p_id", UUID.class));
            profile.setDisplayName(rs.getString("p_display_name"));
            profile.setHeadline(rs.getString("p_headline"));
            profile.setBio(rs.getString("p_bio"));
            profile.setCreatedAt(rs.getObject("p_created_at", LocalDateTime.class));
            profile.setUpdatedAt(rs.getObject("p_updated_at", LocalDateTime.class));
            profile.setVersion(rs.getLong("p_version"));

            UUID educationId = rs.getObject("e_id", UUID.class);
            if (educationId != null) {
                Education education = new Education();
                education.setId(educationId);
                education.setDegree(rs.getString("e_degree"));
                education.setInstitution(rs.getString("e_institution"));
                education.setFieldOfStudy(rs.getString("e_field_of_study"));
                education.setGraduationYear(rs.getObject("e_graduation_year", Integer.class));
                education.setCreatedAt(rs.getObject("e_created_at", LocalDateTime.class));
                education.setUpdatedAt(rs.getObject("e_updated_at", LocalDateTime.class));
                education.setVersion(rs.getLong("e_version"));
                education.setProfile(profile);
                profile.setEducation(education);
            }

            UUID professionId = rs.getObject("pr_id", UUID.class);
            if (professionId != null) {
                Profession profession = new Profession();
                profession.setId(professionId);
                profession.setJobTitle(rs.getString("pr_job_title"));
                profession.setCompany(rs.getString("pr_company"));
                profession.setIndustry(rs.getString("pr_industry"));
                profession.setCreatedAt(rs.getObject("pr_created_at", LocalDateTime.class));
                profession.setUpdatedAt(rs.getObject("pr_updated_at", LocalDateTime.class));
                profession.setVersion(rs.getLong("pr_version"));
                profession.setProfile(profile);
                profile.setProfession(profession);
            }

            UUID locationId = rs.getObject("l_id", UUID.class);
            if (locationId != null) {
                Location location = new Location();
                location.setId(locationId);
                location.setCity(rs.getString("l_city"));
                location.setCountry(rs.getString("l_country"));
                location.setLatitude(rs.getObject("l_latitude", Double.class));
                location.setLongitude(rs.getObject("l_longitude", Double.class));
                location.setCreatedAt(rs.getObject("l_created_at", LocalDateTime.class));
                location.setUpdatedAt(rs.getObject("l_updated_at", LocalDateTime.class));
                location.setVersion(rs.getLong("l_version"));
                location.setProfile(profile);
                profile.setLocation(location);
            }

            UUID lifestyleId = rs.getObject("ls_id", UUID.class);
            if (lifestyleId != null) {
                Lifestyle lifestyle = new Lifestyle();
                lifestyle.setId(lifestyleId);
                lifestyle.setDrinks(rs.getBoolean("ls_drinks"));
                lifestyle.setSmokes(rs.getBoolean("ls_smokes"));
                lifestyle.setReligion(rs.getString("ls_religion"));
                lifestyle.setCreatedAt(rs.getObject("ls_created_at", LocalDateTime.class));
                lifestyle.setUpdatedAt(rs.getObject("ls_updated_at", LocalDateTime.class));
                lifestyle.setVersion(rs.getLong("ls_version"));
                lifestyle.setProfile(profile);
                profile.setLifestyle(lifestyle);
            }

            UUID preferencesId = rs.getObject("pref_id", UUID.class);
            if (preferencesId != null) {
                Preferences preferences = new Preferences();
                preferences.setId(preferencesId);

                String gendersString = rs.getString("pref_preferred_genders");
                if (gendersString != null && !gendersString.trim().isEmpty() && gendersString.length() > 2) {
                    String content = gendersString.substring(1, gendersString.length() - 1);
                    Set<String> gendersSet = java.util.Arrays.stream(content.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(java.util.stream.Collectors.toSet());
                    preferences.setPreferredGenders(gendersSet);
                } else {
                    preferences.setPreferredGenders(new java.util.HashSet<>());
                }

                preferences.setPreferredMinAge(rs.getObject("pref_preferred_min_age", Integer.class));
                preferences.setPreferredMaxAge(rs.getObject("pref_preferred_max_age", Integer.class));
                preferences.setRelationshipType(rs.getString("pref_relationship_type"));
                preferences.setWantsKids(rs.getBoolean("pref_wants_kids"));
                preferences.setOpenToLongDistance(rs.getBoolean("pref_open_to_long_distance"));
                preferences.setCreatedAt(rs.getObject("pref_created_at", LocalDateTime.class));
                preferences.setUpdatedAt(rs.getObject("pref_updated_at", LocalDateTime.class));
                preferences.setVersion(rs.getLong("pref_version"));
                preferences.setProfile(profile);
                profile.setPreferences(preferences);
            }

            UUID userMatchStateId = rs.getObject("ums_id", UUID.class);
            if (userMatchStateId != null) {
                UserMatchState userMatchState = new UserMatchState();
                userMatchState.setId(userMatchStateId);
                userMatchState.setSentToMatchingService(rs.getBoolean("ums_sent_to_matching_service"));
                userMatchState.setProfileComplete(rs.getBoolean("ums_profile_complete"));
                userMatchState.setReadyForMatching(rs.getBoolean("ums_ready_for_matching"));
                userMatchState.setIntent(rs.getString("ums_intent"));
                userMatchState.setGender(rs.getString("ums_gender"));
                userMatchState.setDateOfBirth(rs.getObject("ums_date_of_birth", java.time.LocalDate.class));
                userMatchState.setLastMatchedAt(rs.getObject("ums_last_matched_at", LocalDateTime.class));
                userMatchState.setGroupId(rs.getString("ums_group_id"));
                userMatchState.setCreatedAt(rs.getObject("ums_created_at", LocalDateTime.class));
                userMatchState.setUpdatedAt(rs.getObject("ums_updated_at", LocalDateTime.class));
                userMatchState.setVersion(rs.getLong("ums_version"));
                userMatchState.setProfile(profile);
                profile.setUserMatchState(userMatchState);
            }

            return profile;
        }
    }

    private static final class MediaFileRowMapper implements RowMapper<MediaFile> {
        @Override
        public MediaFile mapRow(ResultSet rs, int rowNum) throws SQLException {
            MediaFile mediaFile = new MediaFile();
            mediaFile.setId(rs.getObject("id", UUID.class));
            mediaFile.setOriginalFileName(rs.getString("original_file_name"));
            mediaFile.setFileType(rs.getString("file_type"));
            String reelTypeStr = rs.getString("reel_type");
            if (reelTypeStr != null) {
                mediaFile.setReelType(ReelType.valueOf(reelTypeStr));
            }
            mediaFile.setFileSize(rs.getLong("file_size"));
            mediaFile.setFilePath(rs.getString("file_path"));
            mediaFile.setDisplayOrder(rs.getInt("display_order"));
            mediaFile.setUploadedAt(rs.getObject("uploaded_at", LocalDateTime.class));
            mediaFile.setVersion(rs.getLong("version"));
            return mediaFile;
        }
    }
}
