package com.dvgs.ration.external;

import static com.dvgs.ration.external.ExternalClientConfig.timeout;

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
public class ExternalRationApiClient {

    private final WebClient webClient;
    private final long timeoutMs;

    public ExternalRationApiClient(WebClient rationExternalWebClient,
                                  @Value("${ration.external.timeout-ms:3000}") long timeoutMs) {
        this.webClient = rationExternalWebClient;
        this.timeoutMs = timeoutMs;
    }

    @Cacheable(cacheNames = "rationCardExternal", key = "#cardNo", unless = "#result == null")
    public Mono<RationCardExternalDto> getCardByNo(String cardNo) {
        return webClient.get()
                .uri("/api/external/ration/cards/{cardNo}", cardNo)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, resp -> Mono.error(new CardNotFoundException(cardNo)))
                .bodyToMono(RationCardExternalDto.class)
                .timeout(timeout(timeoutMs))
                .onErrorMap(WebClientResponseException.NotFound.class, ex -> new CardNotFoundException(cardNo))
                .onErrorMap(TimeoutException.class, ex -> new ExternalTimeoutException())
                .onErrorMap(WebClientRequestException.class, ex -> new ExternalServiceUnavailableException(ex));
    }

    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String cardNo) {
            super("Ration card not found: " + cardNo);
        }
    }

    public static class ExternalTimeoutException extends RuntimeException {
        public ExternalTimeoutException() {
            super("External ration API timed out");
        }
    }

    public static class ExternalServiceUnavailableException extends RuntimeException {
        public ExternalServiceUnavailableException(Throwable cause) {
            super("External ration API unavailable", cause);
        }
    }
}
