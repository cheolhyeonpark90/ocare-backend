package com.ocare.ocarebackend.web.health.dto.inner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ValueUnitDto {

    private String unit;
    private Double value;
}
