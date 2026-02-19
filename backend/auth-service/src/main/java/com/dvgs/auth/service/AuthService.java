package com.dvgs.auth.service;

import com.dvgs.auth.config.AuthProperties;
import com.dvgs.auth.domain.RefreshToken;
import com.dvgs.auth.domain.User;
import com.dvgs.auth.dto.AuthUserView;
import com.dvgs.auth.dto.EligibilityProfileDto;
import com.dvgs.auth.dto.LoginResponse;
import com.dvgs.auth.dto.OtpRequest;
import com.dvgs.auth.dto.OtpVerifyRequest;
import com.dvgs.auth.dto.RefreshTokenRequest;
import com.dvgs.auth.dto.TokenResponse;
import com.dvgs.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final AuthProperties authProperties;

    public void requestOtp(OtpRequest request) {
        otpService.requestOtp(request);
    }

    @Transactional
    public LoginResponse verifyOtp(OtpVerifyRequest request) {
        User user = otpService.verifyOtp(request);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = tokenService.generateAccessToken(user);
        RefreshToken refreshToken = tokenService.createRefreshToken(user, request.getDeviceId());

        Instant accessExpires = Instant.now().plus(authProperties.getToken().getAccessTtl());
        return userMapper.toLoginResponse(user,
                accessToken,
                refreshToken.getToken(),
                accessExpires,
                refreshToken.getExpiresAt());
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());
        String accessToken = tokenService.generateAccessToken(refreshToken.getUser());
        return tokenService.buildTokenResponse(accessToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());
        tokenService.revokeRefreshToken(refreshToken);
        if (request.getDeviceId() != null) {
            tokenService.revokeDeviceTokens(refreshToken.getUser(), request.getDeviceId());
        }
    }

    public AuthUserView getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toView(user);
    }

    public EligibilityProfileDto getEligibilityProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toEligibilityProfile(user);
    }
}
