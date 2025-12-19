package com.ocare.ocarebackend.domain.health.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthLogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "health-log-topic-v1";

    public void send(HealthLogMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, message.getRecordKey(), jsonMessage);
            log.info("Sending Message to Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize HealthLogMessage", e);
        }
    }
}
