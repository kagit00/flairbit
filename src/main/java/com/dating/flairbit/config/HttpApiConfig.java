package com.dating.flairbit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpApiProperties.class)
public class HttpApiConfig {
}
