package com.dating.flairbit.dto;

import lombok.*;

/**
 * The type Jwt response.
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class LogInResponse {
    private String token;
    private long expiry;
}