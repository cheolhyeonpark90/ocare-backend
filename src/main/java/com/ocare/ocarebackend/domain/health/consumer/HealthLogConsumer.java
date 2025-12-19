package com.ocare.ocarebackend.domain.health.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import com.ocare.ocarebackend.domain.health.service.HealthLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthLogConsumer {

    private final HealthLogService healthLogService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "health-log-topic-v1", groupId = "ocare-group")
    public void consume(java.util.List<String> messages) {
        log.info("Polled batch of {} messages.", messages.size());

        java.util.List<HealthLogMessage> logMessages = new java.util.ArrayList<>();

        for (String message : messages) {
            try {
                HealthLogMessage logMessage = objectMapper.readValue(message, HealthLogMessage.class);
                logMessages.add(logMessage);
            } catch (Exception e) {
                log.error("Failed to map message: {}", message, e);
            }
        }

        if (!logMessages.isEmpty()) {
            healthLogService.saveAllBatch(logMessages);
        }
    }
}
