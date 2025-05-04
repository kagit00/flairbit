package com.dating.flairbit.validation;

import com.dating.flairbit.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
public final class GenericValidationUtility {
    private GenericValidationUtility() {
        throw new UnsupportedOperationException("unsupported");
    }

    public static String validatePayload(ConsumerRecord<String, String> consumerRecord) {
        String payload = consumerRecord.value();
        if (payload == null) {
            log.error("Received null payload on topic: {}", consumerRecord.topic());
            throw new BadRequestException("Received null payload from topic: " + consumerRecord.topic());
        }
        return payload;
    }
}
