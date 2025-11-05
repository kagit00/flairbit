package com.dating.flairbit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsSendRequest {
    private UUID sessionId;
    private String senderEmail;
    private String intent;
    private String content;
    @NotNull
    private UUID clientMessageId;
}