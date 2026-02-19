package com.dvgs.scheme.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(AuthServiceProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public EligibilityProfileDto getMyEligibilityProfile(String bearerToken) {
        return restClient.get()
                .uri("/api/users/me/eligibility-profile")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(EligibilityProfileDto.class);
    }
}
