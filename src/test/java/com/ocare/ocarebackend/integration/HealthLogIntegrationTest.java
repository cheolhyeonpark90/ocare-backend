package com.ocare.ocarebackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.web.health.dto.HealthLogRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedKafka(topics = { "health-log-topic-v1", "health-log-topic-v1.DLT" }, partitions = 1)
@org.springframework.test.context.TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
class HealthLogIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @MockitoBean
    private org.springframework.data.redis.core.HashOperations<String, Object, Object> hashOperations;

    @Test
    @DisplayName("End-to-End: Controller -> Kafka -> Service -> DB")
    @WithMockUser(username = "it-user", roles = "USER")
    void shouldProcessHealthLogSuccessfully() throws Exception {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.hasKey(any())).thenReturn(true);

        String json = "{" +
                "\"recordkey\": \"it-test-key\"," +
                "\"data\": {" +
                "  \"entries\": [" +
                "    {" +
                "      \"period\": { \"from\": \"2024-12-25 15:00:00\" }," +
                "      \"steps\": 500," +
                "      \"distance\": { \"value\": 0.5, \"unit\": \"km\" }," +
                "      \"calories\": { \"value\": 50, \"unit\": \"kcal\" }" +
                "    }" +
                "  ]" +
                "}" +
                "}";

        mockMvc.perform(post("/api/health/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var log = healthLogRepository.findByRecordKeyAndMeasuredAt("it-test-key",
                    LocalDateTime.of(2024, 12, 25, 15, 0));
            assertThat(log).isPresent();
            assertThat(log.get().getSteps()).isEqualTo(500);
        });
    }
}
