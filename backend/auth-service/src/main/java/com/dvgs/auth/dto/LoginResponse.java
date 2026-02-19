package com.dvgs.auth.dto;

import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    Instant accessTokenExpiresAt;
    Instant refreshTokenExpiresAt;
    String tokenType;
    String userId;
    String fullName;
    String phoneNumber;
    String email;
    Set<String> roles;
}
