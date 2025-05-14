package com.dating.flairbit.async.consumers;

import com.dating.flairbit.async.FlairBitProducer;
import com.dating.flairbit.dto.KafkaListenerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseKafkaConsumer {
    private final FlairBitProducer dlqProducer;
    private List<KafkaListenerConfig> listenerConfigs;

    @Autowired
    public void setListenerConfigs(List<KafkaListenerConfig> listenerConfigs) {
        this.listenerConfigs = listenerConfigs;
    }

    public void consume(ConsumerRecord<String, String> consumerRecord, KafkaListenerConfig config) {
        String payload = consumerRecord.value();
        if (payload == null) {
            log.warn("Invalid payload for key={} on topic={}. Sending to DLQ.", consumerRecord.key(), consumerRecord.topic());
            dlqProducer.sendMessage(config.getDlqTopic(), consumerRecord.key(), consumerRecord.value());
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                config.getPayloadProcessor().process(payload).join();
            } catch (Exception e) {
                log.error("Failed to process payload for key={} on topic={}: {}", consumerRecord.key(), consumerRecord.topic(), e.getMessage(), e);
                dlqProducer.sendMessage(config.getDlqTopic(), consumerRecord.key(), consumerRecord.value());
            }
        }).exceptionally(throwable -> {
            log.error("Async processing failed for key={} on topic={}: {}", consumerRecord.key(), consumerRecord.topic(), throwable.getMessage(), throwable);
            return null;
        });
    }

    protected List<KafkaListenerConfig> getListenerConfigs() {
        return listenerConfigs;
    }
}