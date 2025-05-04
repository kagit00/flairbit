package com.dating.flairbit.connector;

public interface ThirdPartyConnector<T, R> {
    boolean supports(String integrationKey);
    R call(T request);
}
