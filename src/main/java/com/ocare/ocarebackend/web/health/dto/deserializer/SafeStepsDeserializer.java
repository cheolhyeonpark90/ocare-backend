package com.ocare.ocarebackend.web.health.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class SafeStepsDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            String value = p.getText();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            double doubleValue = Double.parseDouble(value);
            return (int) Math.round(doubleValue);
        } catch (NumberFormatException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
