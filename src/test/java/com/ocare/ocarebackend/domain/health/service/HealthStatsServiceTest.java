package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthStatsServiceTest {

    @InjectMocks
    private HealthStatsService healthStatsService;

    @Mock
    private HealthLogRepository healthLogRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Test
    @DisplayName("Cache Hit - Return Data from Redis")
    void getDailyStats_CacheHit() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(Map.of("2024-12-25", "100")); // Mock data for steps, dist,
                                                                                           // cal

        // When
        List<HealthStatsSummary> result = healthStatsService.getDailyStats("test-key");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPeriod()).isEqualTo("2024-12-25");
        verify(healthLogRepository, never()).findDailyStatsByRecordKey(anyString()); // DB Should Not Be Called
    }

    @Test
    @DisplayName("Cache Miss - Call DB and Update Redis")
    void getDailyStats_CacheMiss() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(anyString())).thenReturn(Collections.emptyMap()); // Cache Empty

        HealthStatsDto dbResult = new HealthStatsDto("2024-12-25", 100L, 1.0, 100.0);
        when(healthLogRepository.findDailyStatsByRecordKey(anyString())).thenReturn(List.of(dbResult));

        // When
        List<HealthStatsSummary> result = healthStatsService.getDailyStats("test-key");

        // Then
        assertThat(result).hasSize(1);
        verify(healthLogRepository).findDailyStatsByRecordKey("test-key");
        verify(hashOperations, org.mockito.Mockito.times(3)).putAll(anyString(), any(Map.class)); // Verify Cache Put
                                                                                                  // (Steps, Dist, Cal)
        verify(redisTemplate, org.mockito.Mockito.times(3)).expire(anyString(), any(Duration.class)); // Verify TTL Set
    }
}
