package com.dvgs.gateway;

import java.net.InetSocketAddress;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingConfig {

    /**
     * Default key resolver for rate limiting.
     *
     * Strategy:
     * - If authenticated: use principal name (userId)
     * - Else: use client IP (from X-Forwarded-For when present)
     */
    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(p -> "user:" + p.getName())
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + clientIp(exchange.getRequest())));
    }

    private String clientIp(ServerHttpRequest request) {
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // first IP in the list is the original client
            return xff.split(",")[0].trim();
        }
        InetSocketAddress remote = request.getRemoteAddress();
        return remote != null && remote.getAddress() != null ? remote.getAddress().getHostAddress() : "unknown";
    }
}
