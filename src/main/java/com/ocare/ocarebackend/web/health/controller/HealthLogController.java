package com.ocare.ocarebackend.web.health.controller;

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

    @PostMapping
    public ResponseEntity<String> receiveHealthLogs(@RequestBody HealthLogRequestDto request) {
        if (request.getData() == null || request.getData().getEntries() == null) {
            return ResponseEntity.badRequest().body("No data entries found");
        }

        List<HealthLogEntryDto> validEntries = request.getData().getEntries().stream()
                .filter(HealthLogEntryDto::isValid)
                .collect(Collectors.toList());

        int total = request.getData().getEntries().size();
        int valid = validEntries.size();
        int skipped = total - valid;

        log.info("Received Health Logs - RecordKey: {}, Total: {}, Valid: {}, Skipped: {}",
                request.getRecordKey(), total, valid, skipped);

        // 실제 구현 시 validEntries를 DB에 저장

        return ResponseEntity.ok("Processed " + valid + " valid entries.");
    }
}
