package com.dating.flairbit.service;

public interface AlertService {
    void notifyFailure(String subject, String message, Throwable cause);
}
