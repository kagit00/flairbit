package com.dating.flairbit.repo;

import com.dating.flairbit.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySessionIdOrderBySentAtDesc(UUID sessionId, org.springframework.data.domain.Pageable pageable);
}