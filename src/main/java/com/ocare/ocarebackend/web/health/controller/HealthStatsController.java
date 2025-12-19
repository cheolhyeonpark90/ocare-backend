package com.ocare.ocarebackend.web.health.controller;

import com.ocare.ocarebackend.domain.health.service.HealthStatsService;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class HealthStatsController {

    private final HealthStatsService healthStatsService;

    @GetMapping("/daily/{recordKey}")
    public ResponseEntity<List<HealthStatsSummary>> getDailyStats(@PathVariable String recordKey) {
        List<HealthStatsSummary> stats = healthStatsService.getDailyStats(recordKey);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/monthly/{recordKey}")
    public ResponseEntity<List<HealthStatsSummary>> getMonthlyStats(@PathVariable String recordKey) {
        List<HealthStatsSummary> stats = healthStatsService.getMonthlyStats(recordKey);
        return ResponseEntity.ok(stats);
    }
}
