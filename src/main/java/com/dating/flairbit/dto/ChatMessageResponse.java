package com.dating.flairbit.dto;

import com.dating.flairbit.models.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private UUID messageId;
    private UUID sessionId;
    private UUID senderId;
    private String senderDisplayName;
    private String content;
    private Instant sentAt;
    private boolean delivered;
    private boolean seen;

    public static ChatMessageResponse from(ChatMessage m) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.setMessageId(m.getId());
        r.setSessionId(m.getSession().getId());
        r.setSenderId(m.getSender().getId());
        r.setSenderDisplayName(m.getSender().getDisplayName());
        r.setContent(m.getContent());
        r.setSentAt(m.getSentAt());
        r.setDelivered(m.isDelivered());
        r.setSeen(m.isSeen());
        return r;
    }
}