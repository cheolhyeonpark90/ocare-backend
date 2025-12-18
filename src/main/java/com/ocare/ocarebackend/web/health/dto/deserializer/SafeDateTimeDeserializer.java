package com.ocare.ocarebackend.web.health.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SafeDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER_A = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_B = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, FORMATTER_A);
        } catch (DateTimeParseException e1) {
            try {
                return OffsetDateTime.parse(value, FORMATTER_B).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                try {
                    return OffsetDateTime.parse(value).toLocalDateTime();
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }
}
