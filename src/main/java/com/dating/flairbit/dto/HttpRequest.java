package com.dating.flairbit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequest {
    @NotBlank
    private String integrationKey;
    @NotBlank
    private String method;
    @NotBlank
    private String path;
    private Object body;
    private Map<String, String> customHeaders;
}

