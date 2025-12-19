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

    @jakarta.validation.constraints.NotBlank(message = "recordKey must not be blank")
    @JsonProperty("recordkey")
    private String recordKey;

    private HealthLogDataDto data;
}
