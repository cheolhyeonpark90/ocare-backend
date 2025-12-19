package com.ocare.ocarebackend.web.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.web.health.dto.HealthLogEntryDto;
import com.ocare.ocarebackend.web.health.dto.HealthLogRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealthLogParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Complex JSON Input Parsing Test")
    void parseComplexJson() throws Exception {
        // Given
        String json = "{\n" +
                "    \"recordkey\" : \"7836887b-b12a-440f...\",\n" +
                "    \"data\": {\n" +
                "        \"entries\": [\n" +
                "            {\n" +
                "                \"period\": {\n" +
                "                    \"from\": \"2024-11-15 00:00:00\",\n" +
                "                    \"to\": \"2024-11-15 00:10:00\"\n" +
                "                },\n" +
                "                \"distance\": {\n" +
                "                    \"unit\": \"km\",\n" +
                "                    \"value\": 0.04223\n" +
                "                },\n" +
                "                \"calories\": {\n" +
                "                    \"unit\": \"kcal\",\n" +
                "                    \"value\": 2.03\n" +
                "                },\n" +
                "                \"steps\": 54\n" +
                "            },\n" +
                "            {\n" +
                "                \"period\": {\n" +
                "                    \"from\": \"2024-11-14T21:30:00+0000\",\n" +
                "                    \"to\": \"2024-11-14T21:40:00+0000\"\n" +
                "                },\n" +
                "                \"distance\": {\n" +
                "                    \"unit\": \"km\",\n" +
                "                    \"value\": 0.05\n" +
                "                },\n" +
                "                \"calories\": {\n" +
                "                    \"unit\": \"kcal\",\n" +
                "                    \"value\": 3.5\n" +
                "                },\n" +
                "                \"steps\": \"688.55\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"period\": {\n" +
                "                   \"from\": \"invalid date\"\n" +
                "                },\n" +
                "                \"steps\": \"invalid step\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        // When
        HealthLogRequestDto requestDto = objectMapper.readValue(json, HealthLogRequestDto.class);

        // Then
        assertThat(requestDto).isNotNull();
        assertThat(requestDto.getRecordKey()).isEqualTo("7836887b-b12a-440f...");

        List<HealthLogEntryDto> entries = requestDto.getData().getEntries();
        assertThat(entries).hasSize(3);

        // 1. 정상 포맷 데이터
        HealthLogEntryDto entry1 = entries.get(0);
        assertThat(entry1.getSteps()).isEqualTo(54);
        assertThat(entry1.getMeasuredAt()).isEqualTo(LocalDateTime.of(2024, 11, 15, 0, 0, 0));
        assertThat(entry1.getDistance().getValue()).isEqualTo(0.04223);
        assertThat(entry1.isValid()).isTrue();

        // 2. 비정상 포맷 대응 (실수형 스텝 문자열 & ISO 날짜 포맷)
        HealthLogEntryDto entry2 = entries.get(1);
        // "688.55" -> 689 (반올림)
        assertThat(entry2.getSteps()).isEqualTo(689);
        // "2024-11-14T21:30:00+0000" -> 2024-11-14 21:30:00 (Offset 그대로 로컬 시간 변환)
        assertThat(entry2.getMeasuredAt()).isEqualTo(LocalDateTime.of(2024, 11, 14, 21, 30, 0));
        assertThat(entry2.isValid()).isTrue();

        // 3. 완전히 잘못된 데이터 (파싱 실패 -> null 처리 -> 유효성 검사 실패)
        HealthLogEntryDto entry3 = entries.get(2);
        assertThat(entry3.getSteps()).isNull();
        assertThat(entry3.getMeasuredAt()).isNull();
        assertThat(entry3.isValid()).isFalse();
    }
}
