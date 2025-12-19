package com.ocare.ocarebackend.domain.health.dto;

public interface HealthStatsSummary {
    String getPeriod();

    Long getSteps();

    Double getDistance();

    Double getCalories();
}
