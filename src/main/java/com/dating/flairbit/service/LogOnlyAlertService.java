package com.dating.flairbit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogOnlyAlertService implements AlertService {
    @Override
    public void notifyFailure(String subject, String message, Throwable cause) {
        log.warn("ALERT: {}\n{}\nRoot Cause: {}", subject, message, cause.getMessage());
    }
}
