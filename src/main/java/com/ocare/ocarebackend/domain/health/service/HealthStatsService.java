package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthStatsService {

    private final HealthLogRepository healthLogRepository;

    public List<HealthStatsSummary> getDailyStats(String recordKey) {
        return healthLogRepository.findDailyStatsByRecordKey(recordKey);
    }

    public List<HealthStatsSummary> getMonthlyStats(String recordKey) {
        return healthLogRepository.findMonthlyStatsByRecordKey(recordKey);
    }
}
