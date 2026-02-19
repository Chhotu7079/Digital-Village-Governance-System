package com.dvgs.ration;

import com.dvgs.ration.external.ExternalRationApiClient.CardNotFoundException;
import com.dvgs.ration.external.ExternalRationApiClient.ExternalServiceUnavailableException;
import com.dvgs.ration.external.ExternalRationApiClient.ExternalTimeoutException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardNotFoundException.class)
    public Mono<Map<String, Object>> handleNotFound(CardNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(error(HttpStatus.NOT_FOUND, "RATION_CARD_NOT_FOUND", ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(ExternalTimeoutException.class)
    public Mono<Map<String, Object>> handleExternalTimeout(ExternalTimeoutException ex, ServerWebExchange exchange) {
        return Mono.just(error(HttpStatus.GATEWAY_TIMEOUT, "RATION_EXTERNAL_TIMEOUT", ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public Mono<Map<String, Object>> handleExternalUnavailable(ExternalServiceUnavailableException ex, ServerWebExchange exchange) {
        return Mono.just(error(HttpStatus.BAD_GATEWAY, "RATION_EXTERNAL_UNAVAILABLE", ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<Map<String, Object>> handleBadRequest(IllegalArgumentException ex, ServerWebExchange exchange) {
        return Mono.just(error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<Map<String, Object>> handleGeneric(Exception ex, ServerWebExchange exchange) {
        // Include exception type for easier debugging (safe: no stack trace, no sensitive data)
        String msg = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage());
        return Mono.just(error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", msg, exchange.getRequest().getPath().value()));
    }

    private Map<String, Object> error(HttpStatus status, String code, String message, String path) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", Instant.now().toString());
        m.put("code", code);
        m.put("message", message);
        m.put("path", path);
        return m;
    }
}
