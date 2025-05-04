package com.dating.flairbit.dto;

import lombok.*;

/**
 * The type Jwt request.
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogInRequest {
    private String username;
    private String password;
}
