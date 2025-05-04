package com.dating.flairbit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "third-party.connectors")
@Data
public class HttpApiProperties {
    private Map<String, SingleApiConfig> configs = new HashMap<>();

    @Data
    public static class SingleApiConfig {
        private String baseUrl;
        private String apiKey;
        private Map<String, String> headers = new HashMap<>();
    }
}

