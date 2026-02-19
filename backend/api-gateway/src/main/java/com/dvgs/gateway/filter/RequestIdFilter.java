package com.dvgs.gateway.filter;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String ATTR = "requestId";

    @Override
    public int getOrder() {
        // Run early
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        exchange.getAttributes().put(ATTR, requestId);

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(HEADER, requestId)
                .build();

        exchange.getResponse().getHeaders().set(HEADER, requestId);

        final String rid = requestId;

        // Put into Reactor Context so other filters/logging can access
        return chain.filter(exchange.mutate().request(mutated).build())
                .contextWrite(ctx -> ctx.put(ATTR, rid));
    }
}
