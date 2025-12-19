package com.ocare.ocarebackend.domain.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatsDto implements HealthStatsSummary {
    private String period;
    private Long steps;
    private Double distance;
    private Double calories;
}
