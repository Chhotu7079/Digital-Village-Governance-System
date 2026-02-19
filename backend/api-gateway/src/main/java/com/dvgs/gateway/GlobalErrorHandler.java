package com.dvgs.gateway;

import com.dvgs.gateway.filter.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse.Context;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalErrorHandler {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext,
            ObjectMapper objectMapper
    ) {
        return (ServerWebExchange exchange, Throwable ex) -> {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (ex instanceof ResponseStatusException rse) {
                status = HttpStatus.valueOf(rse.getStatusCode().value());
            }

            String code = switch (status.value()) {
                case 404 -> "NOT_FOUND";
                case 429 -> "RATE_LIMITED";
                default -> status.is4xxClientError() ? "BAD_REQUEST" : "INTERNAL_ERROR";
            };

            String message = switch (status.value()) {
                case 404 -> "Route not found";
                case 429 -> "Too many requests";
                default -> ex.getMessage() != null ? ex.getMessage() : "Unexpected error";
            };

            String requestId = (String) exchange.getAttributes().get(RequestIdFilter.ATTR);
            if (requestId == null || requestId.isBlank()) {
                requestId = exchange.getRequest().getHeaders().getFirst(RequestIdFilter.HEADER);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("code", code);
            body.put("message", message);
            body.put("path", exchange.getRequest().getURI().getRawPath());
            if (requestId != null && !requestId.isBlank()) {
                body.put("requestId", requestId);
            }

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
        };
    }
}
