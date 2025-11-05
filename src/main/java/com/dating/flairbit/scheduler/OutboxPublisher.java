package com.dating.flairbit.scheduler;

import com.dating.flairbit.dto.ChatMessageResponse;
import com.dating.flairbit.models.ChatMessageOutbox;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.ChatMessageJDBCRepository;
import com.dating.flairbit.repo.ChatMessageOutboxJDBCRepository;
import com.dating.flairbit.repo.ChatSessionJDBCRepository;
import com.dating.flairbit.service.chats.ChatSessionService;
import com.dating.flairbit.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class OutboxPublisher {

    private final ChatMessageOutboxJDBCRepository outboxRepo;
    private final SimpMessagingTemplate messaging;
    private final ObjectMapper json;

    public OutboxPublisher(
            ChatMessageOutboxJDBCRepository outboxRepo,
            SimpMessagingTemplate messaging,
            @Qualifier("objectMapper") ObjectMapper json
    ) {
        this.outboxRepo = outboxRepo;
        this.messaging = messaging;
        this.json = json;
    }

    @Scheduled(fixedDelay = 3_000)
    @Transactional
    public void publish() {
        List<ChatMessageOutbox> batch = outboxRepo.findPending();
        for (ChatMessageOutbox o : batch) {
            try {
                ChatMessageResponse payload = json.readValue(o.getPayload(), ChatMessageResponse.class);
                if (o.getDestination().startsWith("user:")) {
                    String[] p = o.getDestination().split(":", 3);
                    messaging.convertAndSendToUser(p[1], p[2], payload);
                } else {
                    messaging.convertAndSend(o.getDestination(), payload);
                }
                outboxRepo.markProcessed(o.getId(), o.getRetryCount(), true);
            } catch (Exception e) {
                log.error(e.getMessage());
                int retries = o.getRetryCount() + 1;
                outboxRepo.markProcessed(o.getId(), retries, false);
                if (retries > 10) log.error("Outbox failed permanently: {}", o.getId());
            }
        }
    }
}