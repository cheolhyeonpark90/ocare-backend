package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLog;
import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class HealthLogServiceTest {

    @InjectMocks
    private HealthLogService healthLogService;

    @Mock
    private HealthLogRepository healthLogRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Test
    @DisplayName("New Log - Should Insert DB and Increment Redis with full value")
    void saveHealthLog_NewLog() {
        // Given
        HealthLogMessage msg = createMessage(100, 1.0, 100.0);
        when(healthLogRepository.findByRecordKeyAndMeasuredAt(any(), any())).thenReturn(Optional.empty());
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.hasKey(any())).thenReturn(true);

        // When
        healthLogService.saveHealthLog(msg);

        // Then
        verify(healthLogRepository).save(any(HealthLog.class));
        // Verify Redis Increments (Delta = Full Value)
        verify(hashOperations).increment(any(), eq("2024-12-25"), eq(100L)); // Steps
        verify(hashOperations).increment(any(), eq("2024-12-25"), eq(1.0)); // Distance
        verify(hashOperations).increment(any(), eq("2024-12-25"), eq(100.0)); // Calories
    }

    @Test
    @DisplayName("Update Log - Should Update DB and Increment Redis with Delta")
    void saveHealthLog_UpdateLog() {
        // Given
        HealthLogMessage msg = createMessage(120, 1.2, 120.0); // New Value
        HealthLog existing = HealthLog.builder()
                .steps(100).distance(1.0).calories(100.0).build(); // Old Value

        when(healthLogRepository.findByRecordKeyAndMeasuredAt(any(), any())).thenReturn(Optional.of(existing));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.hasKey(any())).thenReturn(true);

        // When
        healthLogService.saveHealthLog(msg);

        // Then
        // DB Update checked implicitly by logic coverage, or we can spy existing entity
        // Verify Redis Increments (Delta = 120 - 100 = 20)
        verify(hashOperations).increment(any(), eq("2024-12-25"), eq(20L)); // Steps
        verify(hashOperations).increment(any(), eq("2024-12-25"),
                org.mockito.ArgumentMatchers.doubleThat(d -> Math.abs(d - 0.2) < 0.001)); // Distance (approx)
        verify(hashOperations).increment(any(), eq("2024-12-25"), eq(20.0)); // Calories
    }

    private HealthLogMessage createMessage(Integer steps, Double distance, Double calories) {
        HealthLogMessage msg = new HealthLogMessage();
        msg.setRecordKey("test-key");
        msg.setMeasuredAt(LocalDateTime.of(2024, 12, 25, 10, 0));
        msg.setSteps(steps);
        msg.setDistanceValue(distance);
        msg.setDistanceUnit("km");
        msg.setCaloriesValue(calories);
        msg.setCaloriesUnit("kcal");
        return msg;
    }
}
