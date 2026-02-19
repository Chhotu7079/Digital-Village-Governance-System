package com.dvgs.land.external;

import static com.dvgs.land.external.ExternalClientConfig.timeout;

import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class ExternalLandApiClient {

    private final WebClient webClient;
    private final long timeoutMs;

    public ExternalLandApiClient(WebClient landExternalWebClient,
                                @Value("${land.external.timeout-ms:3000}") long timeoutMs) {
        this.webClient = landExternalWebClient;
        this.timeoutMs = timeoutMs;
    }

    @Cacheable(cacheNames = "landRecordExternal", key = "#key", unless = "#result == null")
    public Mono<LandRecordExternalDto> search(String key, LandSearchParams params) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/external/land/records/search")
                        .queryParam("district", params.district())
                        .queryParam("anchal", params.anchal())
                        .queryParam("mauza", params.mauza())
                        .queryParam("khataNo", params.khataNo())
                        .queryParam("khesraNo", params.khesraNo())
                        .build())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new RecordNotFoundException(params)))
                .bodyToMono(LandRecordExternalDto.class)
                .timeout(timeout(timeoutMs))
                .onErrorMap(WebClientResponseException.NotFound.class, ex -> new RecordNotFoundException(params))
                .onErrorMap(TimeoutException.class, ex -> new ExternalTimeoutException())
                .onErrorMap(WebClientRequestException.class, ex -> new ExternalServiceUnavailableException(ex));
    }

    public record LandSearchParams(String district, String anchal, String mauza, String khataNo, String khesraNo) {}

    public static class RecordNotFoundException extends RuntimeException {
        public RecordNotFoundException(LandSearchParams params) {
            super("Land record not found");
        }
    }

    public static class ExternalTimeoutException extends RuntimeException {
        public ExternalTimeoutException() {
            super("External land API timed out");
        }
    }

    public static class ExternalServiceUnavailableException extends RuntimeException {
        public ExternalServiceUnavailableException(Throwable cause) {
            super("External land API unavailable", cause);
        }
    }
}
