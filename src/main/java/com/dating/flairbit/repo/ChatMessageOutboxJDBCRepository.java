package com.dating.flairbit.repo;

import com.dating.flairbit.models.ChatMessageOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatMessageOutboxJDBCRepository {

    private final JdbcTemplate jdbc;

    private static final String SQL_INSERT = """
        INSERT INTO chat_message_outbox (id, payload, destination)
        VALUES (gen_random_uuid(), ?::jsonb, ?)
        """;

    private static final String SQL_FIND_PENDING = """
        SELECT id, payload, destination, retry_count
        FROM chat_message_outbox
        WHERE sent_at IS NULL
        ORDER BY created_at
        LIMIT 100
        FOR UPDATE SKIP LOCKED
        """;

    private static final String SQL_MARK_SENT = """
        UPDATE chat_message_outbox SET sent_at = ?, retry_count = ?
        WHERE id = ?
        """;

    public void saveAll(List<ChatMessageOutbox> outbox) {
        if (outbox.isEmpty()) return;

        jdbc.batchUpdate(SQL_INSERT, outbox, 100, (ps, o) -> {
            ps.setString(1, o.getPayload());
            ps.setString(2, o.getDestination());
        });
    }

    public List<ChatMessageOutbox> findPending() {
        return jdbc.query(SQL_FIND_PENDING, (rs, i) -> {
            ChatMessageOutbox o = new ChatMessageOutbox();
            o.setId(rs.getObject("id", UUID.class));
            o.setPayload(rs.getString("payload"));
            o.setDestination(rs.getString("destination"));
            o.setRetryCount(rs.getInt("retry_count"));
            return o;
        });
    }

    public void markProcessed(UUID id, int retryCount, boolean success) {
        jdbc.update(SQL_MARK_SENT,
                success ? Timestamp.from(Instant.now()) : null,
                retryCount,
                id
        );
    }
}