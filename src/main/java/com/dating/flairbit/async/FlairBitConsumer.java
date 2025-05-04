package com.dating.flairbit.async;

import com.dating.flairbit.processor.FlairBitPayloadProcessor;
import com.dating.flairbit.validation.GenericValidationUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class FlairBitConsumer {

    private static final String USERS_TRANSFER_JOB_STATUS_TOPIC = "flairbit-users-transfer-job-status-retrieval";
    private static final String REEL_INTERACTION_RECORD_TOPIC = "reel-interaction-record";
    private final FlairBitPayloadProcessor flairBitPayloadProcessor;

    @KafkaListener(
            topics = "${kafka.topics.flairbit.matches:flairbit-match-suggestions}",
            groupId = "${spring.kafka.consumer.group-id:flairbit-consumer-group}",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "4"
    )
    @Transactional
    public  void consumeImportedMatches(ConsumerRecord<String, String> consumerRecord) {
        try {
            GenericValidationUtility.validatePayload(consumerRecord);
            String payload = consumerRecord.value();
            flairBitPayloadProcessor.processImportedMatchesPayload(payload);

        } catch (Exception e) {
            log.error("Failed to process match: error={}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = USERS_TRANSFER_JOB_STATUS_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:flairbit-consumer-group}",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "2"
    )
    @Transactional
    public void consumeUsersTransferJobStatus(ConsumerRecord<String, String> consumerRecord) {
        GenericValidationUtility.validatePayload(consumerRecord);
        String payload = consumerRecord.value();
        flairBitPayloadProcessor.processUsersTransferJobStatusPayload(payload);
    }

    @KafkaListener(
            topics = REEL_INTERACTION_RECORD_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:flairbit-consumer-group}",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    @Transactional
    public void recordReelInteraction(ConsumerRecord<String, String> consumerRecord) {
        GenericValidationUtility.validatePayload(consumerRecord);
        String payload = consumerRecord.value();
        flairBitPayloadProcessor.processReelInteractionPayload(payload);
    }
}
