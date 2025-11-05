package com.dating.flairbit.dto;

import com.dating.flairbit.models.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {
    private UUID sessionId;
    private ProfileChatDto profile1;
    private ProfileChatDto profile2;

    public static ChatSessionResponse of(ChatSession s) {
        return new ChatSessionResponse(s.getId(), ProfileChatDto.from(s.getProfile1()), ProfileChatDto.from(s.getProfile2()));
    }
}