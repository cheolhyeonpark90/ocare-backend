package com.ocare.ocarebackend.web.health.dto.inner;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ocare.ocarebackend.web.health.dto.deserializer.SafeDateTimeDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PeriodDto {

    @JsonDeserialize(using = SafeDateTimeDeserializer.class)
    private LocalDateTime from;

    @JsonDeserialize(using = SafeDateTimeDeserializer.class)
    private LocalDateTime to;
}
