package com.dvgs.auth.controller;

import com.dvgs.auth.dto.AuthUserView;
import com.dvgs.auth.dto.LoginResponse;
import com.dvgs.auth.dto.OtpRequest;
import com.dvgs.auth.dto.OtpVerifyRequest;
import com.dvgs.auth.dto.RefreshTokenRequest;
import com.dvgs.auth.dto.TokenResponse;
import com.dvgs.auth.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/otp/request")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestOtp(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login/otp/verify")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserView> me(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(authService.getUserProfile(userId));
    }
}
