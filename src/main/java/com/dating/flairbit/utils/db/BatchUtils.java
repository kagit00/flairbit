package com.dating.flairbit.utils.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BatchUtils {

    private BatchUtils() {

    }

    public static <T> void processInBatches(List<T> items, int batchSize, Consumer<List<T>> consumer) {
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, end);
            consumer.accept(batch);
        }
    }

    public static <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    public static <T> CompletableFuture<Void> processInBatchesAsync(List<T> items, int batchSize, Function<List<T>, CompletableFuture<Void>> processor, Executor executor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, end);
            futures.add(CompletableFuture.runAsync(() -> processor.apply(batch), executor));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}