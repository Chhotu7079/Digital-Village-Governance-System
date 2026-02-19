package com.dvgs.gateway.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory.Config;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Fail-open wrapper for Spring Cloud Gateway's RequestRateLimiter.
 *
 * If Redis is down or the rate limiter throws, the request is allowed to proceed.
 */
@Component("FailOpenRequestRateLimiter")
public class FailOpenRequestRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private static final Logger log = LoggerFactory.getLogger(FailOpenRequestRateLimiterGatewayFilterFactory.class);

    private final RequestRateLimiterGatewayFilterFactory delegate;
    private final boolean failOpen;

    public FailOpenRequestRateLimiterGatewayFilterFactory(
            RequestRateLimiterGatewayFilterFactory delegate,
            @org.springframework.beans.factory.annotation.Value("${gateway.ratelimit.fail-open:true}") boolean failOpen
    ) {
        this.delegate = delegate;
        this.failOpen = failOpen;
    }

    @Override
    public GatewayFilter apply(Config config) {
        GatewayFilter filter = delegate.apply(config);
        return (exchange, chain) -> filter.filter(exchange, chain)
                .onErrorResume(ex -> {
                    if (!failOpen) {
                        return Mono.error(ex);
                    }
                    log.warn("Rate limiter failed (fail-open). Allowing request. path={} error={}",
                            exchange.getRequest().getURI().getRawPath(), ex.toString());
                    return chain.filter(exchange);
                });
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }
}
