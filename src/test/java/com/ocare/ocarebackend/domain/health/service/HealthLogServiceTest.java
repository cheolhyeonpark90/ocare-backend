package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthLogServiceTest {

    @InjectMocks
    private HealthLogService healthLogService;

    @Mock
    private HealthLogPersister healthLogPersister;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    @DisplayName("Fast Path - Should Call saveBatchTransactional")
    void saveAllBatch_FastPath() {
        // Given
        HealthLogMessage msg = createMessage(100, 1.0, 100.0);
        List<HealthLogMessage> messages = List.of(msg);

        // When
        healthLogService.saveAllBatch(messages);

        // Then
        verify(healthLogPersister).saveBatchTransactional(messages);
        verify(healthLogPersister, never()).saveOne(any());
    }

    @Test
    @DisplayName("Fallback Path - Should Call saveOne and DLT on failure")
    void saveAllBatch_FallbackPath() throws Exception {
        // Given
        HealthLogMessage msg1 = createMessage(100, 1.0, 100.0); // Fail
        HealthLogMessage msg2 = createMessage(200, 2.0, 200.0); // Success
        List<HealthLogMessage> messages = List.of(msg1, msg2);

        // Mock: Batch Fail
        doThrow(new RuntimeException("Batch Error")).when(healthLogPersister).saveBatchTransactional(messages);
        // Mock: Msg1 Fail
        doThrow(new RuntimeException("Msg1 Error")).when(healthLogPersister).saveOne(msg1);

        // Mock: Kafka DLT Send
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        // KafkaTemplate.send returns CompletableFuture in Spring Boot 3 / Spring Kafka
        // 3
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        // When
        healthLogService.saveAllBatch(messages);

        // Then
        verify(healthLogPersister).saveBatchTransactional(messages);
        verify(healthLogPersister).saveOne(msg1);
        verify(healthLogPersister).saveOne(msg2);
        // Verify DLT for msg1
        verify(kafkaTemplate).send(eq("health-log-topic-v1.DLT"), eq(msg1.getRecordKey()), any());
    }

    private HealthLogMessage createMessage(Integer steps, Double distance, Double calories) {
        HealthLogMessage msg = new HealthLogMessage();
        msg.setRecordKey("test-key-" + steps);
        msg.setMeasuredAt(LocalDateTime.of(2024, 12, 25, 10, 0));
        msg.setSteps(steps);
        msg.setDistanceValue(distance);
        msg.setDistanceUnit("km");
        msg.setCaloriesValue(calories);
        msg.setCaloriesUnit("kcal");
        return msg;
    }
}
