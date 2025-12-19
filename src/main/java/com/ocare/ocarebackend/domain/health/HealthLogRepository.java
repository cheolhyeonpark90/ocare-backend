package com.ocare.ocarebackend.domain.health;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    Optional<HealthLog> findByRecordKeyAndMeasuredAt(String recordKey, LocalDateTime measuredAt);
}
