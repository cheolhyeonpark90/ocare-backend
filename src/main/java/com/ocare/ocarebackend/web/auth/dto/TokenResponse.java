package com.ocare.ocarebackend.web.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private final String tokenType = "Bearer";
}
