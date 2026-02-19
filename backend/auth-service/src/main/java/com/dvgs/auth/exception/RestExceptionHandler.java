package com.dvgs.auth.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<Map<String, Object>> handleOtp(OtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("OTP_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleRefresh(RefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error("REFRESH_TOKEN_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("AUTH_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(error("INVALID_REQUEST", ex.getMessage()));
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of(
                "code", code,
                "message", message,
                "timestamp", Instant.now().toString()
        );
    }
}
