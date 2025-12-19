package com.ocare.ocarebackend.domain.health.dto;

public interface HealthStatsSummary {
    String getPeriod();

    Long getTotalSteps();

    Double getTotalDistance();

    Double getTotalCalories();
}
