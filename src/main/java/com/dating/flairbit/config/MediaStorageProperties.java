package com.dating.flairbit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "media.storage")
public class MediaStorageProperties {
    private String uploadFolder = "uploads/";
    private long maxFileSize = 50 * 1024 * 1024;
    private List<String> allowedTypes = List.of("image/jpeg", "image/png", "video/mp4");
}