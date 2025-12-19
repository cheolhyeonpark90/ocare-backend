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
    public void consume(String message) {
        log.info("Raw MQ Message: {}", message);
        try {
            HealthLogMessage logMessage = objectMapper.readValue(message, HealthLogMessage.class);
            healthLogService.saveHealthLog(logMessage);
            log.info("Consumed & Saved: {} at {}", logMessage.getRecordKey(), logMessage.getMeasuredAt());
        } catch (Exception e) {
            log.error("Failed to consume message", e);
        }
    }
}
