package com.ocare.ocarebackend.web.health.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthLogRequestDto {

    @JsonProperty("recordKey")
    private String recordKey;

    private HealthLogDataDto data;
}
