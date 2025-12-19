package com.ocare.ocarebackend.domain.health.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthLogService {

    private final HealthLogPersister healthLogPersister;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String DLT_TOPIC = "health-log-topic-v1.DLT";

    public void saveAllBatch(List<HealthLogMessage> messages) {
        try {
            healthLogPersister.saveBatchTransactional(messages);
        } catch (Exception e) {
            log.warn("Batch processing failed. Switching to single item processing. Error: {}", e.getMessage());
            fallbackToSingleProcessing(messages);
        }
    }

    public void fallbackToSingleProcessing(List<HealthLogMessage> messages) {
        int successCount = 0;
        int failCount = 0;

        for (HealthLogMessage msg : messages) {
            try {
                healthLogPersister.saveOne(msg);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("Failed to save individual log: {}, Error: {}", msg, e.getMessage());
                sendToDlt(msg, e.getMessage());
            }
        }
        log.info("Fallback processing complete. Success: {}, Failed: {}", successCount, failCount);
    }

    private void sendToDlt(HealthLogMessage msg, String errorMessage) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send(DLT_TOPIC, msg.getRecordKey(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send to DLT", ex);
                        } else {
                            log.info("Sent to DLT: {}", msg.getRecordKey());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for DLT", e);
        }
    }
}
