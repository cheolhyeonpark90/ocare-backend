package com.ocare.ocarebackend.web.health.controller;

import com.ocare.ocarebackend.domain.health.dto.HealthLogMessage;
import com.ocare.ocarebackend.domain.health.producer.HealthLogProducer;
import com.ocare.ocarebackend.web.health.dto.HealthLogEntryDto;
import com.ocare.ocarebackend.web.health.dto.HealthLogRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/health/logs")
@RequiredArgsConstructor
public class HealthLogController {

    private final HealthLogProducer healthLogProducer;

    @PostMapping
    public ResponseEntity<String> receiveHealthLogs(@RequestBody HealthLogRequestDto request) {
        if (request.getData() == null || request.getData().getEntries() == null) {
            return ResponseEntity.badRequest().body("No data entries found");
        }

        List<HealthLogEntryDto> validEntries = request.getData().getEntries().stream()
                .filter(HealthLogEntryDto::isValid)
                .collect(Collectors.toList());

        for (HealthLogEntryDto entry : validEntries) {
            HealthLogMessage message = HealthLogMessage.builder()
                    .recordKey(request.getRecordKey())
                    .steps(entry.getSteps())
                    .measuredAt(entry.getMeasuredAt())
                    .distanceValue(entry.getDistance() != null ? entry.getDistance().getValue() : 0.0)
                    .distanceUnit(entry.getDistance() != null ? entry.getDistance().getUnit() : null)
                    .caloriesValue(entry.getCalories() != null ? entry.getCalories().getValue() : 0.0)
                    .caloriesUnit(entry.getCalories() != null ? entry.getCalories().getUnit() : null) // Typo fix in
                                                                                                      // getter usage if
                                                                                                      // any, assume
                                                                                                      // Getter is
                                                                                                      // standard
                    .build();

            healthLogProducer.send(message);
        }

        int total = request.getData().getEntries().size();
        int valid = validEntries.size();
        int skipped = total - valid;

        log.info("Received Health Logs - RecordKey: {}, Total: {}, Queued: {}, Skipped: {}",
                request.getRecordKey(), total, valid, skipped);

        return ResponseEntity.ok("Queued " + valid + " entries successfully.");
    }
}
