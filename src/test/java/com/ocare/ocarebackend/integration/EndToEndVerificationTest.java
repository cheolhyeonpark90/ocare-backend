package com.ocare.ocarebackend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.domain.health.service.HealthStatsService;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedKafka(topics = { "health-log-topic-v1", "health-log-topic-v1.DLT" }, partitions = 1)
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.max-poll-records=500"
})
class EndToEndVerificationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private HealthStatsService healthStatsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Load Input Files -> Ingest -> Wait -> Export Results")
    @org.springframework.security.test.context.support.WithMockUser(username = "e2e-user")
    void processAllInputFilesAndExportResults() throws Exception {
        // 1. Prepare Paths
        Path inputDir = Paths.get("input");
        Path outputDir = Paths.get("output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 2. Read Input Files
        List<File> jsonFiles = Arrays
                .asList(Objects.requireNonNull(inputDir.toFile().listFiles((dir, name) -> name.endsWith(".json"))));
        jsonFiles.sort(Comparator.comparing(File::getName));

        Set<String> recordKeys = new HashSet<>();
        List<Map<String, Object>> allResults = new ArrayList<>();

        // 3. Ingest Data
        System.out.println(">>> Starting Data Ingestion...");
        for (File file : jsonFiles) {
            String content = Files.readString(file.toPath());

            JsonNode root = objectMapper.readTree(content);
            String recordKey = root.path("recordkey").asText();
            if (recordKey == null || recordKey.isBlank()) {
                recordKey = root.path("recordKey").asText();
            }
            if (recordKey != null && !recordKey.isBlank()) {
                recordKeys.add(recordKey);
            }

            System.out.println("Processing file: " + file.getName() + ", recordKey: " + recordKey);

            mockMvc.perform(post("/api/health/logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                            .csrf()))
                    .andExpect(status().isOk());
        }
        System.out.println(">>> Ingestion Complete. Waiting for Async Processing (15s)...");

        // 4. Wait for Kafka Processing
        Thread.sleep(15000);

        // 5. Verification & Export
        System.out.println(">>> Starting Result Export...");
        for (String key : recordKeys) {
            Map<String, Object> keyResult = new HashMap<>();
            keyResult.put("recordKey", key);

            try {
                // Fetch All Monthly Stats
                List<HealthStatsSummary> monthlyStats = healthStatsService.getMonthlyStats(key);
                keyResult.put("monthly", monthlyStats);
            } catch (Exception e) {
                keyResult.put("monthly", "Error: " + e.getMessage());
            }

            try {
                // Fetch All Daily Stats
                List<HealthStatsSummary> dailyStats = healthStatsService.getDailyStats(key);
                keyResult.put("daily", dailyStats);
            } catch (Exception e) {
                keyResult.put("daily", "Error: " + e.getMessage());
            }

            allResults.add(keyResult);
        }

        // 6. Write Output File
        File resultFile = outputDir.resolve("verification_result.json").toFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(resultFile, allResults);

        System.out.println(">>> Export Complete: " + resultFile.getAbsolutePath());
    }
}
