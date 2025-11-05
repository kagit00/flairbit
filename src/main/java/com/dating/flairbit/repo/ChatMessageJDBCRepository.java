package com.dating.flairbit.repo;

import com.dating.flairbit.models.ChatMessage;
import com.dating.flairbit.models.ChatSession;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMessageJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_UPSERT_MESSAGE = """
    INSERT INTO chat_messages 
    (id, session_id, sender_profile_id, content, delivered, seen, sent_at, client_msg_id)
    VALUES (gen_random_uuid(), ?, ?, ?, false, false, ?, ?)
    ON CONFLICT (client_msg_id) DO NOTHING
    RETURNING id, session_id, sender_profile_id, content, delivered, seen, sent_at, client_msg_id
    """;

    public ChatMessage save(ChatMessage msg) {
        return jdbcTemplate.query(SQL_UPSERT_MESSAGE, rs -> {
                    if (!rs.next()) {
                        log.info("Duplicate message ignored: {}", msg.getClientMsgId());
                        return findByClientMsgId(msg.getClientMsgId())
                                .orElseThrow(() -> new IllegalStateException("Message not found after conflict"));
                    }
                    return mapRow(rs, 1);
                },
                msg.getSession().getId(),
                msg.getSender().getId(),
                msg.getContent(),
                Timestamp.from(msg.getSentAt()),
                msg.getClientMsgId()
        );
    }

    private static final String SQL_FIND_BY_CLIENT_MSG_ID = """
        SELECT id, session_id, sender_profile_id, content, delivered, seen, sent_at, client_msg_id
        FROM chat_messages
        WHERE client_msg_id = ?
        """;

    public Optional<ChatMessage> findByClientMsgId(UUID clientMsgId) {
        List<ChatMessage> results = jdbcTemplate.query(
                SQL_FIND_BY_CLIENT_MSG_ID,
                new ChatMessageRowMapper(),
                clientMsgId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }


    private static final String SQL_EXISTS_BY_CLIENT_MSG_ID = """
        SELECT COUNT(*) FROM chat_messages WHERE client_msg_id = ?
        """;

    public boolean existsByClientMsgId(UUID clientMsgId) {
        Integer count = jdbcTemplate.queryForObject(
                SQL_EXISTS_BY_CLIENT_MSG_ID,
                Integer.class,
                clientMsgId
        );
        return count != null && count > 0;
    }

    private static final String SQL_FIND_BY_ID = """
        SELECT 
            m.id, m.session_id, m.sender_profile_id, m.content, m.delivered, m.seen, m.sent_at, m.client_msg_id,
            s.id as s_id,
            p.id as p_id, p.user_id as p_user_id,
            u.id as u_id, u.email as u_email
        FROM chat_messages m
        JOIN chat_sessions s ON m.session_id = s.id
        JOIN profiles p ON m.sender_profile_id = p.id
        JOIN users u ON p.user_id = u.id
        WHERE m.id = ?
        """;

    public Optional<ChatMessage> findById(UUID id) {
        List<ChatMessage> results = jdbcTemplate.query(SQL_FIND_BY_ID, new ChatMessageFullRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }


    private static final String SQL_FIND_BY_SESSION_ID = """
        SELECT 
            m.id, m.session_id, m.sender_profile_id, m.content, m.delivered, m.seen, m.sent_at, m.client_msg_id,
            p.id as p_id, p.user_id as p_user_id,
            u.id as u_id, u.email as u_email
        FROM chat_messages m
        JOIN profiles p ON m.sender_profile_id = p.id
        JOIN users u ON p.user_id = u.id
        WHERE m.session_id = ?
        ORDER BY m.sent_at DESC
        LIMIT ?
        """;

    public List<ChatMessage> findBySessionIdOrderBySentAtDesc(UUID sessionId, int limit) {
        return jdbcTemplate.query(
                SQL_FIND_BY_SESSION_ID,
                new ChatMessageWithProfileRowMapper(),
                sessionId, limit
        );
    }


    private static final String SQL_FIND_UNSEEN = """
        SELECT 
            m.id, m.session_id, m.sender_profile_id, m.content, m.delivered, m.seen, m.sent_at, m.client_msg_id
        FROM chat_messages m
        WHERE m.session_id = ? 
          AND m.seen = false 
          AND m.sender_profile_id != ?
        """;

    public List<ChatMessage> findUnseenBySessionAndReader(UUID sessionId, UUID readerProfileId) {
        return jdbcTemplate.query(
                SQL_FIND_UNSEEN,
                new ChatMessageRowMapper(),
                sessionId, readerProfileId
        );
    }


    private static class ChatMessageRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage msg = new ChatMessage();
            msg.setId(rs.getObject("id", UUID.class));
            msg.setSession(ChatSession.builder().id(rs.getObject("session_id", UUID.class)).build());
            msg.setSender(Profile.builder().id(rs.getObject("sender_profile_id", UUID.class)).build());
            msg.setContent(rs.getString("content"));
            msg.setDelivered(rs.getBoolean("delivered"));
            msg.setSeen(rs.getBoolean("seen"));
            msg.setSentAt(rs.getTimestamp("sent_at").toInstant());
            msg.setClientMsgId(rs.getObject("client_msg_id", UUID.class));
            return msg;
        }
    }

    private static class ChatMessageWithProfileRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage msg = new ChatMessage();
            msg.setId(rs.getObject("id", UUID.class));
            msg.setSession(ChatSession.builder().id(rs.getObject("session_id", UUID.class)).build());

            Profile sender = new Profile();
            sender.setId(rs.getObject("p_id", UUID.class));
            sender.setUser(User.builder()
                    .id(rs.getObject("u_id", UUID.class))
                    .email(rs.getString("u_email")).build()
            );
            msg.setSender(sender);

            msg.setContent(rs.getString("content"));
            msg.setDelivered(rs.getBoolean("delivered"));
            msg.setSeen(rs.getBoolean("seen"));
            msg.setSentAt(rs.getTimestamp("sent_at").toInstant());
            msg.setClientMsgId(rs.getObject("client_msg_id", UUID.class));
            return msg;
        }
    }

    private static class ChatMessageFullRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage msg = new ChatMessage();
            msg.setId(rs.getObject("id", UUID.class));
            msg.setSession(ChatSession.builder().id(rs.getObject("s_id", UUID.class)).build());

            Profile sender = new Profile();
            sender.setId(rs.getObject("p_id", UUID.class));
            sender.setUser(User.builder()
                    .id(rs.getObject("u_id", UUID.class))
                    .email(rs.getString("u_email")).build()
            );
            msg.setSender(sender);

            msg.setContent(rs.getString("content"));
            msg.setDelivered(rs.getBoolean("delivered"));
            msg.setSeen(rs.getBoolean("seen"));
            msg.setSentAt(rs.getTimestamp("sent_at").toInstant());
            msg.setClientMsgId(rs.getObject("client_msg_id", UUID.class));
            return msg;
        }
    }

    private ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ChatMessageRowMapper().mapRow(rs, rowNum);
    }
}