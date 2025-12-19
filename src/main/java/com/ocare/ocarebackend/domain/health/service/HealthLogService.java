package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLog;
import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthLogService {

    private final HealthLogRepository healthLogRepository;

    @Transactional
    public void saveHealthLog(HealthLogMessage msg) {
        Double normalizedDistance = normalizeDistance(msg.getDistanceValue(), msg.getDistanceUnit());
        Double normalizedCalories = normalizeCalories(msg.getCaloriesValue(), msg.getCaloriesUnit());

        Optional<HealthLog> existingLog = healthLogRepository.findByRecordKeyAndMeasuredAt(msg.getRecordKey(),
                msg.getMeasuredAt());

        if (existingLog.isPresent()) {
            existingLog.get().update(msg.getSteps(), normalizedDistance, normalizedCalories);
        } else {
            HealthLog newLog = HealthLog.builder()
                    .recordKey(msg.getRecordKey())
                    .measuredAt(msg.getMeasuredAt())
                    .steps(msg.getSteps())
                    .distance(normalizedDistance)
                    .calories(normalizedCalories)
                    .build();
            healthLogRepository.save(newLog);
        }
    }

    private Double normalizeDistance(Double value, String unit) {
        if (value == null)
            return 0.0;
        if ("m".equalsIgnoreCase(unit)) {
            return value / 1000.0;
        }
        return value;
    }

    private Double normalizeCalories(Double value, String unit) {
        if (value == null)
            return 0.0;
        if ("cal".equalsIgnoreCase(unit)) {
            return value / 1000.0;
        }
        return value;
    }
}
