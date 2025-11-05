package com.dating.flairbit.repo;

import com.dating.flairbit.models.ChatSession;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatSessionJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_FIND_BY_ID_WITH_PROFILES = """
        SELECT 
            s.id, s.intent, s.created_at,
            p1.id as p1_id, p1.user_id as p1_user_id,
            u1.id as u1_id, u1.email as u1_email,
            p2.id as p2_id, p2.user_id as p2_user_id,
            u2.id as u2_id, u2.email as u2_email
        FROM chat_sessions s
        JOIN profiles p1 ON s.profile1_id = p1.id
        JOIN users u1 ON p1.user_id = u1.id
        JOIN profiles p2 ON s.profile2_id = p2.id
        JOIN users u2 ON p2.user_id = u2.id
        WHERE s.id = ?
        """;

    private static final String SQL_FIND_BY_PROFILES_AND_INTENT = """
        SELECT id, profile1_id, profile2_id, intent, created_at
        FROM chat_sessions
        WHERE profile1_id = ? AND profile2_id = ? AND intent = ?
        """;

    private static final String SQL_UPSERT_SESSION = """
        INSERT INTO chat_sessions (id, profile1_id, profile2_id, intent, created_at)
        VALUES (gen_random_uuid(), ?, ?, ?, now())
        ON CONFLICT (profile1_id, profile2_id, intent) DO NOTHING
        RETURNING id, profile1_id, profile2_id, intent, created_at
        """;

    public Optional<ChatSession> findByIdWithProfiles(UUID sessionId) {
        return jdbcTemplate.query(SQL_FIND_BY_ID_WITH_PROFILES, new ChatSessionWithProfilesRowMapper(), sessionId)
                .stream().findFirst();
    }

    public ChatSession getOrCreateSession(UUID profile1Id, UUID profile2Id, String intent) {
        List<ChatSession> existing = jdbcTemplate.query(
                SQL_FIND_BY_PROFILES_AND_INTENT,
                new ChatSessionRowMapper(),
                profile1Id, profile2Id, intent
        );

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        List<ChatSession> inserted = jdbcTemplate.query(
                SQL_UPSERT_SESSION,
                new ChatSessionRowMapper(),
                profile1Id, profile2Id, intent
        );

        if (!inserted.isEmpty()) {
            return inserted.get(0);
        }

        return jdbcTemplate.queryForObject(
                SQL_FIND_BY_PROFILES_AND_INTENT,
                new ChatSessionRowMapper(),
                profile1Id, profile2Id, intent
        );
    }


    private static final class ChatSessionRowMapper implements RowMapper<ChatSession> {
        @Override
        public ChatSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSession session = new ChatSession();
            session.setId(rs.getObject("id", UUID.class));
            session.setIntent(rs.getString("intent"));
            session.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            return session;
        }
    }

    private static final class ChatSessionWithProfilesRowMapper implements RowMapper<ChatSession> {
        @Override
        public ChatSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSession session = new ChatSession();
            session.setId(rs.getObject("id", UUID.class));
            session.setIntent(rs.getString("intent"));
            session.setCreatedAt(rs.getTimestamp("created_at").toInstant());

            Profile p1 = new Profile();
            p1.setId(rs.getObject("p1_id", UUID.class));
            User u1 = new User();
            u1.setId(rs.getObject("u1_id", UUID.class));
            u1.setEmail(rs.getString("u1_email"));
            p1.setUser(u1);
            session.setProfile1(p1);

            Profile p2 = new Profile();
            p2.setId(rs.getObject("p2_id", UUID.class));
            User u2 = new User();
            u2.setId(rs.getObject("u2_id", UUID.class));
            u2.setEmail(rs.getString("u2_email"));
            p2.setUser(u2);
            session.setProfile2(p2);

            return session;
        }
    }
}