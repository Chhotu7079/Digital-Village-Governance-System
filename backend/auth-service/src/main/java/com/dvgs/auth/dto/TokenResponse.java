package com.dvgs.auth.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenResponse {
    String accessToken;
    Instant accessTokenExpiresAt;
    String tokenType;
}
