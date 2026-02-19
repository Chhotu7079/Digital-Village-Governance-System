package com.dvgs.auth.service;

import com.dvgs.auth.config.AuthProperties;
import com.dvgs.auth.domain.RefreshToken;
import com.dvgs.auth.domain.User;
import com.dvgs.auth.dto.TokenResponse;
import com.dvgs.auth.repository.RefreshTokenRepository;
import com.dvgs.auth.exception.RefreshTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;
    private final SecretKey secretKey;
    private final MacAlgorithm macAlgorithm = Jwts.SIG.HS256;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(authProperties.getToken().getAccessTtl());
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .issuer(authProperties.getToken().getIssuer())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .signWith(secretKey, macAlgorithm)
                .compact();
    }

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceId) {
        if (deviceId != null) {
            refreshTokenRepository.deleteByUserAndDeviceId(user, deviceId);
        }
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .deviceId(deviceId)
                .expiresAt(Instant.now().plus(authProperties.getToken().getRefreshTtl()))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    public TokenResponse buildTokenResponse(String accessToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(Instant.now().plus(authProperties.getToken().getAccessTtl()))
                .tokenType("Bearer")
                .build();
    }

    public RefreshToken validateRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));
        if (token.isRevoked() || Instant.now().isAfter(token.getExpiresAt())) {
            throw new RefreshTokenException("Refresh token expired or revoked");
        }
        return token;
    }

    public void revokeRefreshToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeDeviceTokens(User user, String deviceId) {
        refreshTokenRepository.deleteByUserAndDeviceId(user, deviceId);
    }
}
