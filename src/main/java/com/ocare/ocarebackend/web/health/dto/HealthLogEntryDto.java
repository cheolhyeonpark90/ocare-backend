package com.ocare.ocarebackend.web.health.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ocare.ocarebackend.web.health.dto.deserializer.SafeStepsDeserializer;
import com.ocare.ocarebackend.web.health.dto.inner.PeriodDto;
import com.ocare.ocarebackend.web.health.dto.inner.ValueUnitDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class HealthLogEntryDto {

    private PeriodDto period;

    private ValueUnitDto distance;

    private ValueUnitDto calories;

    @JsonDeserialize(using = SafeStepsDeserializer.class)
    private Integer steps;

    public LocalDateTime getMeasuredAt() {
        return period != null ? period.getFrom() : null;
    }

    public boolean isValid() {
        return steps != null && getMeasuredAt() != null;
    }
}
