package com.ocare.ocarebackend.domain.health;

import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
        Optional<HealthLog> findByRecordKeyAndMeasuredAt(String recordKey, LocalDateTime measuredAt);

        @Query("SELECT " +
                        "FUNCTION('DATE_FORMAT', h.measuredAt, '%Y-%m-%d') as period, " +
                        "SUM(h.steps) as totalSteps, " +
                        "SUM(h.distance) as totalDistance, " +
                        "SUM(h.calories) as totalCalories " +
                        "FROM HealthLog h " +
                        "WHERE h.recordKey = :recordKey " +
                        "GROUP BY FUNCTION('DATE_FORMAT', h.measuredAt, '%Y-%m-%d') " +
                        "ORDER BY period ASC")
        List<HealthStatsSummary> findDailyStatsByRecordKey(@Param("recordKey") String recordKey);

        @Query("SELECT " +
                        "FUNCTION('DATE_FORMAT', h.measuredAt, '%Y-%m') as period, " +
                        "SUM(h.steps) as totalSteps, " +
                        "SUM(h.distance) as totalDistance, " +
                        "SUM(h.calories) as totalCalories " +
                        "FROM HealthLog h " +
                        "WHERE h.recordKey = :recordKey " +
                        "GROUP BY FUNCTION('DATE_FORMAT', h.measuredAt, '%Y-%m') " +
                        "ORDER BY period ASC")
        List<HealthStatsSummary> findMonthlyStatsByRecordKey(@Param("recordKey") String recordKey);
}
