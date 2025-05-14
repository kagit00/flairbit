package com.dating.flairbit.async;

import com.dating.flairbit.async.consumers.BaseKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class FlairBitConsumer extends BaseKafkaConsumer {

    public FlairBitConsumer(FlairBitProducer dlqProducer) {
        super(dlqProducer);
    }

    @KafkaListener(
            topicPattern = "#{@usersTransferJobStatusConfig.topicPattern}",
            groupId = "#{@usersTransferJobStatusConfig.groupId}",
            concurrency = "#{@usersTransferJobStatusConfig.concurrency}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUsersExportStatus(ConsumerRecord<String, String> consumerRecord) {
        consume(consumerRecord, getListenerConfigs().get(0));
    }


    @KafkaListener(
            topicPattern = "#{@matchSuggestionsImportConfig.topicPattern}",
            groupId = "#{@matchSuggestionsImportConfig.groupId}",
            concurrency = "#{@matchSuggestionsImportConfig.concurrency}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMachSuggestions(ConsumerRecord<String, String> consumerRecord) {
        consume(consumerRecord, getListenerConfigs().get(1));
    }

    @KafkaListener(
            topicPattern = "#{@matchSuggestionsImportConfig.topicPattern}",
            groupId = "#{@matchSuggestionsImportConfig.groupId}",
            concurrency = "#{@matchSuggestionsImportConfig.concurrency}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void recordReelInteraction(ConsumerRecord<String, String> consumerRecord) {
        consume(consumerRecord, getListenerConfigs().get(2));
    }
}

