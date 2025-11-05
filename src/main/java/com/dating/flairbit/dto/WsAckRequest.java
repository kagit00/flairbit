package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsAckRequest {
    private UUID messageId;
    private String readerEmail;
    private String intent;
}