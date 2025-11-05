package com.dating.flairbit.service.chats;

import com.dating.flairbit.dto.ChatMessageResponse;
import com.dating.flairbit.dto.ChatSessionResponse;
import com.dating.flairbit.models.*;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.ChatMessageJDBCRepository;
import com.dating.flairbit.repo.ChatMessageOutboxJDBCRepository;
import com.dating.flairbit.repo.ChatSessionJDBCRepository;
import com.dating.flairbit.service.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import java.util.UUID;

@Lazy
@Slf4j
@Service
@Transactional
public class ChatService {
    private final ChatSessionJDBCRepository sessionRepo;
    private final ChatMessageJDBCRepository msgRepo;
    private final ChatMessageOutboxJDBCRepository outboxRepo;
    private final ProfileProcessor profileProcessor;
    private final UserService userService;
    private final ChatSessionService sessionService;
    private final ObjectMapper json;
    private final SimpMessagingTemplate messaging;


    public ChatService(
            ChatSessionJDBCRepository sessionRepo,
            ChatMessageJDBCRepository msgRepo,
            ChatMessageOutboxJDBCRepository outboxRepo,
            ProfileProcessor profileProcessor,
            UserService userService,
            ChatSessionService sessionService,
            @Qualifier("objectMapper") ObjectMapper json,
            SimpMessagingTemplate messaging
    ) {
        this.sessionRepo = sessionRepo;
        this.msgRepo = msgRepo;
        this.outboxRepo = outboxRepo;
        this.profileProcessor = profileProcessor;
        this.userService = userService;
        this.sessionService = sessionService;
        this.json = json;
        this.messaging = messaging;
    }

    public ChatSessionResponse initChat(String fromEmail, String toEmail, String intent) {
        ChatSession session = sessionService.getOrCreateSession(fromEmail, toEmail, intent);
        return ChatSessionResponse.of(session);
    }

    public ChatMessage sendMessage(UUID sessionId, String senderEmail, String intent,
                                   String content, UUID clientMessageId) {

        if (msgRepo.existsByClientMsgId(clientMessageId)) {
            log.info("Ignoring duplicate message: {}", clientMessageId);
            return null;
        }

        ChatSession session = sessionRepo.findByIdWithProfiles(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        User senderUser = userService.getUserByEmail(canonicalEmail(senderEmail));
        Profile senderProfile = profileProcessor.getProfile(senderUser, intent);

        if (!session.getProfile1().getId().equals(senderProfile.getId()) &&
                !session.getProfile2().getId().equals(senderProfile.getId())) {
            throw new SecurityException("Sender not part of session");
        }

        ChatMessage msg = ChatMessage.builder()
                .session(session)
                .sender(senderProfile)
                .content(content)
                .sentAt(Instant.now())
                .delivered(false)
                .seen(false)
                .clientMsgId(clientMessageId)
                .build();

        msg = msgRepo.save(msg);

        ChatMessageResponse payload = ChatMessageResponse.from(msg);

        String topic = "/topic/session." + session.getId();
        String user1Id = session.getProfile1().getUser().getId().toString();
        String user2Id = session.getProfile2().getUser().getId().toString();

        outboxRepo.saveAll(List.of(
                outbox(topic, payload),
                outbox("user:" + user1Id + ":/queue/messages", payload),
                outbox("user:" + user2Id + ":/queue/messages", payload)
        ));

        return msg;
    }

    public void markDelivered(UUID messageId, String readerEmail, String intent) {
        ChatMessage msg = msgRepo.findById(messageId).orElseThrow(() -> new EntityNotFoundException("Message not found"));
        User user = userService.getUserByEmail(canonicalEmail(readerEmail));
        Profile reader = profileProcessor.getProfile(user, intent);
        if (msg.getSender().getId().equals(reader.getId())) return;

        boolean changed = false;
        if (!msg.isDelivered()) { msg.setDelivered(true); changed = true; }
        if (!msg.isSeen()) { msg.setSeen(true); changed = true; }
        if (changed) {
            msgRepo.save(msg);
            messaging.convertAndSend("/topic/session." + msg.getSession().getId(), ChatMessageResponse.from(msg));
        }
    }

    public void markRead(UUID messageId, String readerEmail, String intent) {
        ChatMessage msg = msgRepo.findById(messageId).orElseThrow(() -> new EntityNotFoundException("Message not found"));
        User user = userService.getUserByEmail(canonicalEmail(readerEmail));
        Profile reader = profileProcessor.getProfile(user, intent);
        if (msg.getSender().getId().equals(reader.getId())) return;
        if (!msg.isSeen()) {
            msg.setSeen(true);
            msgRepo.save(msg);
            messaging.convertAndSend("/topic/session." + msg.getSession().getId(), ChatMessageResponse.from(msg));
        }
    }

    public List<ChatMessage> getHistory(UUID sessionId, int limit) {
        return msgRepo.findBySessionIdOrderBySentAtDesc(sessionId, limit);
    }

    private void verifyParticipant(ChatSession session, Profile p) {
        if (!session.getProfile1().getId().equals(p.getId()) && !session.getProfile2().getId().equals(p.getId())) {
            throw new SecurityException("Profile not part of session");
        }
    }

    private String canonicalEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public List<ChatMessage> getUnread(UUID sessionId, String readerEmail, String intent) {
        Profile reader = profileProcessor.getProfile(userService.getUserByEmail(canonicalEmail(readerEmail)), intent);
        return msgRepo.findUnseenBySessionAndReader(sessionId, reader.getId());
    }

    private ChatMessageOutbox outbox(String destination, ChatMessageResponse payload) {
        ChatMessageOutbox o = new ChatMessageOutbox();
        o.setDestination(destination);
        try {
            o.setPayload(json.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        return o;
    }
}