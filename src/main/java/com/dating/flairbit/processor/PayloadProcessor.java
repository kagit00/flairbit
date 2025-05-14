package com.dating.flairbit.processor;

import java.util.concurrent.CompletableFuture;

public interface PayloadProcessor {
    CompletableFuture<Void> process(String payload);
}