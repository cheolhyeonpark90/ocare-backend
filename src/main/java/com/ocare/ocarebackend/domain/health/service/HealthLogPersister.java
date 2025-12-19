package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLog;
import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthLogPersister {

    private final HealthLogRepository healthLogRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatchTransactional(List<HealthLogMessage> messages) {
        for (HealthLogMessage msg : messages) {
            processLogEntry(msg);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOne(HealthLogMessage msg) {
        processLogEntry(msg);
    }

    private void processLogEntry(HealthLogMessage msg) {
        Double normalizedDistance = normalizeDistance(msg.getDistanceValue(), msg.getDistanceUnit());
        Double normalizedCalories = normalizeCalories(msg.getCaloriesValue(), msg.getCaloriesUnit());

        Optional<HealthLog> existingLog = healthLogRepository.findByRecordKeyAndMeasuredAt(msg.getRecordKey(),
                msg.getMeasuredAt());

        int stepsDelta;
        double distanceDelta;
        double caloriesDelta;

        if (existingLog.isPresent()) {
            HealthLog log = existingLog.get();
            stepsDelta = (msg.getSteps() != null ? msg.getSteps() : 0) - (log.getSteps() != null ? log.getSteps() : 0);
            distanceDelta = normalizedDistance - (log.getDistance() != null ? log.getDistance() : 0.0);
            caloriesDelta = normalizedCalories - (log.getCalories() != null ? log.getCalories() : 0.0);
            log.update(msg.getSteps(), normalizedDistance, normalizedCalories);
        } else {
            HealthLog newLog = HealthLog.builder()
                    .recordKey(msg.getRecordKey())
                    .measuredAt(msg.getMeasuredAt())
                    .steps(msg.getSteps())
                    .distance(normalizedDistance)
                    .calories(normalizedCalories)
                    .build();
            healthLogRepository.save(newLog);

            stepsDelta = msg.getSteps() != null ? msg.getSteps() : 0;
            distanceDelta = normalizedDistance;
            caloriesDelta = normalizedCalories;
        }

        updateRedisStats(msg.getRecordKey(), msg.getMeasuredAt(), stepsDelta, distanceDelta, caloriesDelta);
    }

    private void updateRedisStats(String recordKey, java.time.LocalDateTime date, int stepsDelta, double distanceDelta,
            double caloriesDelta) {
        String dateStr = date.toLocalDate().toString();
        String monthStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        updateIfKeyExists("stats:daily:" + recordKey + ":steps", dateStr, stepsDelta);
        updateIfKeyExistsFloat("stats:daily:" + recordKey + ":distance", dateStr, distanceDelta);
        updateIfKeyExistsFloat("stats:daily:" + recordKey + ":calories", dateStr, caloriesDelta);

        updateIfKeyExists("stats:monthly:" + recordKey + ":steps", monthStr, stepsDelta);
        updateIfKeyExistsFloat("stats:monthly:" + recordKey + ":distance", monthStr, distanceDelta);
        updateIfKeyExistsFloat("stats:monthly:" + recordKey + ":calories", monthStr, caloriesDelta);
    }

    private void updateIfKeyExists(String key, String field, long delta) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().increment(key, field, delta);
            redisTemplate.expire(key, Duration.ofDays(30));
        }
    }

    private void updateIfKeyExistsFloat(String key, String field, double delta) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().increment(key, field, delta);
            redisTemplate.expire(key, Duration.ofDays(30));
        }
    }

    private Double normalizeDistance(Double value, String unit) {
        if (value == null)
            return 0.0;
        if ("m".equalsIgnoreCase(unit))
            return value / 1000.0;
        return value;
    }

    private Double normalizeCalories(Double value, String unit) {
        if (value == null)
            return 0.0;
        if ("cal".equalsIgnoreCase(unit))
            return value / 1000.0;
        return value;
    }
}
