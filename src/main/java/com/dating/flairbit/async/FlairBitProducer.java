package com.dating.flairbit.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Slf4j
@Component
public class FlairBitProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Executor kafkaCallbackExecutor;

    public FlairBitProducer(@Qualifier("kafkaCallbackExecutor") Executor kafkaCallbackExecutor, KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaCallbackExecutor = kafkaCallbackExecutor;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, String value) {
        long startTime = System.nanoTime();

        kafkaTemplate.send(topic, key, value)
                .whenCompleteAsync((result, ex) -> {
                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;

                    if (ex == null) {
                        log.info("Sent message to {}: key={}, duration={} ms", topic, key, durationMs);

                    } else {
                        log.error("Failed to send to {}: key={}, error={}", topic, key, ex.getMessage(), ex);
                        sendToDlq(key, value);
                    }
                }, kafkaCallbackExecutor);
    }

    private void sendToDlq(String key, String value) {
        long startTime = System.nanoTime();

        kafkaTemplate.send("flairbit-dlq", key, value)
                .whenCompleteAsync((result, ex) -> {
                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;

                    if (ex == null) {
                        log.info("Sent to DLQ: key={}, duration={} ms", key, durationMs);

                    } else {
                        log.error("Failed to send to DLQ: key={}, error={}", key, ex.getMessage(), ex);
                    }
                }, kafkaCallbackExecutor);
    }
}

