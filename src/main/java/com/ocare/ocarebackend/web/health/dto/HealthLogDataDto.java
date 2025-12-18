package com.ocare.ocarebackend.web.health.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthLogDataDto {
    private List<HealthLogEntryDto> entries;
}
