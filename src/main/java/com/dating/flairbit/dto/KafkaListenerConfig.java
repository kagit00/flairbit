package com.dating.flairbit.dto;

import com.dating.flairbit.processor.PayloadProcessor;
import lombok.Data;

@Data
public class KafkaListenerConfig {
    private String topicPattern;
    private String groupId;
    private int concurrency;
    private String dlqTopic;
    private PayloadProcessor payloadProcessor;
}