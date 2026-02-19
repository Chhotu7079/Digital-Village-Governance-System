package com.dvgs.gateway.filter;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger("ACCESS_LOG");

    @Override
    public int getOrder() {
        // After RequestIdFilter, before routing completes
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        ServerHttpRequest req = exchange.getRequest();

        String requestId = (String) exchange.getAttributes().get(RequestIdFilter.ATTR);
        if (requestId == null) {
            requestId = req.getHeaders().getFirst(RequestIdFilter.HEADER);
        }

        String method = req.getMethod() != null ? req.getMethod().name() : "";
        String path = req.getURI().getRawPath();
        String query = req.getURI().getRawQuery();
        String fullPath = query != null && !query.isBlank() ? (path + "?" + query) : path;

        final String rid = requestId;
        final String m = method;
        final String p = fullPath;

        return chain.filter(exchange)
                .doFinally(signal -> {
                    long ms = Duration.between(start, Instant.now()).toMillis();
                    Integer status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    org.springframework.cloud.gateway.route.Route route =
                            exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "";

                    log.info("requestId={} method={} path={} status={} durationMs={} route={}",
                            safe(rid), m, p, status, ms, safe(routeId));
                });
    }

    private String safe(String s) {
        return s == null ? "" : s.replaceAll("[\r\n\t]", " ");
    }
}
