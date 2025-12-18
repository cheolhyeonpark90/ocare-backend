package com.ocare.ocarebackend.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocare.ocarebackend.web.auth.dto.LoginRequest;
import com.ocare.ocarebackend.web.auth.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

        @LocalServerPort
        private int port;

        private final HttpClient client = HttpClient.newHttpClient();
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        @DisplayName("Full Flow: Signup -> Login -> Access Protected Resource")
        void fullSecurityFlow() throws Exception {
                String baseUrl = "http://localhost:" + port + "/api";

                long timestamp = System.currentTimeMillis();
                String uniqueEmail = "security_" + timestamp + "@example.com";

                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setEmail(uniqueEmail);
                signupRequest.setPassword("password");
                signupRequest.setName("Client Security");
                signupRequest.setNickname("ClientSec");

                String signupJson = objectMapper.writeValueAsString(signupRequest);
                HttpRequest signupPost = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/auth/signup"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(signupJson))
                                .build();

                HttpResponse<String> signupResponse = client.send(signupPost, HttpResponse.BodyHandlers.ofString());

                assertThat(signupResponse.statusCode()).isEqualTo(200);

                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail(uniqueEmail);
                loginRequest.setPassword("password");

                String loginJson = objectMapper.writeValueAsString(loginRequest);
                HttpRequest loginPost = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/auth/login"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                                .build();

                HttpResponse<String> loginResponse = client.send(loginPost, HttpResponse.BodyHandlers.ofString());
                assertThat(loginResponse.statusCode()).isEqualTo(200);

                JsonNode loginBody = objectMapper.readTree(loginResponse.body());
                String accessToken = loginBody.get("accessToken").asText();

                String healthLogJson = "{\n" +
                                "    \"recordkey\" : \"test-key-client\",\n" +
                                "    \"data\": {\n" +
                                "        \"entries\": []\n" +
                                "    }\n" +
                                "}";

                HttpRequest logPost = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/health/logs"))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + accessToken)
                                .POST(HttpRequest.BodyPublishers.ofString(healthLogJson))
                                .build();

                HttpResponse<String> logResponse = client.send(logPost, HttpResponse.BodyHandlers.ofString());

                assertThat(logResponse.statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("Access Forbidden without Token")
        void accessWithoutToken() throws Exception {
                String baseUrl = "http://localhost:" + port + "/api";
                String healthLogJson = "{}";

                HttpRequest logPost = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/health/logs"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(healthLogJson))
                                .build();

                HttpResponse<String> response = client.send(logPost, HttpResponse.BodyHandlers.ofString());

                assertThat(response.statusCode()).isEqualTo(403);
        }
}
