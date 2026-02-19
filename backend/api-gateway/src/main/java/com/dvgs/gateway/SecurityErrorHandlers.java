package com.dvgs.gateway;

import com.dvgs.gateway.filter.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityErrorHandlers {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> write(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
    }

    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> write(exchange, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.ATTR);
        if (requestId == null || requestId.isBlank()) {
            requestId = exchange.getRequest().getHeaders().getFirst(RequestIdFilter.HEADER);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("code", code);
        body.put("message", message);
        if (requestId != null && !requestId.isBlank()) {
            body.put("requestId", requestId);
        }
        body.put("path", exchange.getRequest().getURI().getRawPath());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = ("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (requestId != null && !requestId.isBlank()) {
            exchange.getResponse().getHeaders().set(RequestIdFilter.HEADER, requestId);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
