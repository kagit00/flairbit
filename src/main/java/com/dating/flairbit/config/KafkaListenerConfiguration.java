package com.dating.flairbit.config;

import com.dating.flairbit.dto.KafkaListenerConfig;
import com.dating.flairbit.processor.FlairBitPayloadProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaListenerConfiguration {
    private final FlairBitPayloadProcessor payloadProcessor;

    public KafkaListenerConfiguration(FlairBitPayloadProcessor payloadProcessor) {
        this.payloadProcessor = payloadProcessor;
    }

    @Bean
    public KafkaListenerConfig usersTransferJobStatusConfig() {
        KafkaListenerConfig config = new KafkaListenerConfig();
        config.setTopicPattern("flairbit-users-transfer-job-status-retrieval");
        config.setGroupId("flairbit-users-transfer-job-status-retrieval-group");
        config.setConcurrency(4);
        config.setDlqTopic("flairbit-users-transfer-job-status-retrieval-dlq");
        config.setPayloadProcessor(payloadProcessor::processUsersTransferJobStatusPayload);
        return config;
    }

    @Bean
    public KafkaListenerConfig matchSuggestionsImportConfig() {
        KafkaListenerConfig config = new KafkaListenerConfig();
        config.setTopicPattern("flairbit-matches-suggestions");
        config.setGroupId("match-suggestions-import-group");
        config.setConcurrency(4);
        config.setDlqTopic("match-suggestions-import-dlq");
        config.setPayloadProcessor(payloadProcessor::processImportedMatchSuggestionsPayload);
        return config;
    }

    @Bean
    public KafkaListenerConfig reelInteractionConfig() {
        KafkaListenerConfig config = new KafkaListenerConfig();
        config.setTopicPattern("reel-interaction-record");
        config.setGroupId("reel-interaction-record-group");
        config.setConcurrency(4);
        config.setDlqTopic("reel-interaction-record-dlq");
        config.setPayloadProcessor(payloadProcessor::processReelInteractionPayload);
        return config;
    }
}
