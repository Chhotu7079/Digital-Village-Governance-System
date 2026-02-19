package com.dvgs.ration.external;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ExternalClientConfig.class);

    @Bean
    WebClient rationExternalWebClient(
            @Value("${ration.external.base-url}") String baseUrl,
            MeterRegistry registry
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .filter(observabilityFilter(registry))
                .build();
    }

    static Duration timeout(long timeoutMs) {
        return Duration.ofMillis(timeoutMs);
    }

    private static ExchangeFilterFunction observabilityFilter(MeterRegistry registry) {
        return (request, next) -> {
            long startNs = System.nanoTime();
            String method = request.method().name();
            // Only store low-cardinality info (avoid query params)
            String path = request.url().getPath();

            return next.exchange(request)
                    .doOnNext(resp -> {
                        int status = resp.statusCode().value();
                        long durationNs = System.nanoTime() - startNs;

                        Timer.builder("ration.external.http.client")
                                .tag("method", method)
                                .tag("path", path)
                                .tag("status", String.valueOf(status))
                                .register(registry)
                                .record(durationNs, TimeUnit.NANOSECONDS);

                        registry.counter(
                                "ration.external.http.client.count",
                                "method", method,
                                "path", path,
                                "status", String.valueOf(status)
                        ).increment();

                        log.info("external_api_call method={} path={} status={} durationMs={}",
                                method, path, status, Duration.ofNanos(durationNs).toMillis());
                    })
                    .doOnError(ex -> {
                        long durationNs = System.nanoTime() - startNs;

                        Timer.builder("ration.external.http.client")
                                .tag("method", method)
                                .tag("path", path)
                                .tag("status", "IO_ERROR")
                                .register(registry)
                                .record(durationNs, TimeUnit.NANOSECONDS);

                        registry.counter(
                                "ration.external.http.client.count",
                                "method", method,
                                "path", path,
                                "status", "IO_ERROR"
                        ).increment();

                        log.warn("external_api_call_failed method={} path={} durationMs={} error={}",
                                method, path, Duration.ofNanos(durationNs).toMillis(), ex.toString());
                    });
        };
    }
}
