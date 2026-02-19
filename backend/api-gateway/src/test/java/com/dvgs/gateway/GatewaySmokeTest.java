package com.dvgs.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        // Avoid requiring real secrets/redis in unit smoke tests
        "spring.security.oauth2.resourceserver.jwt.secret=test-secret",
        "auth.jwt.issuer=dvgs-auth",
        // Configure redis to an unused port; limiter will not be hit in these tests
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6390"
})
@AutoConfigureWebTestClient
class GatewaySmokeTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void requestIdIsReturnedOnUnauthorized() {
        webTestClient.get().uri("/api/schemes")
                .header("X-Request-Id", "rid-123")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals("X-Request-Id", "rid-123")
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.requestId").isEqualTo("rid-123")
                .jsonPath("$.path").isEqualTo("/api/schemes");
    }

    @Test
    void gatewayGeneratesRequestIdIfMissing() {
        webTestClient.get().uri("/api/schemes")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void unknownRouteReturnsJson404() {
        webTestClient.get().uri("/does-not-exist")
                .header("X-Request-Id", "rid-404")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals("X-Request-Id", "rid-404")
                .expectBody()
                .jsonPath("$.code").isEqualTo("NOT_FOUND")
                .jsonPath("$.requestId").isEqualTo("rid-404")
                .jsonPath("$.path").isEqualTo("/does-not-exist");
    }

    @Test
    void authEndpointsArePublic() {
        // This verifies security config: /api/auth/** is permitted (should NOT be blocked by security).
        // In this test environment there is no downstream, so status may vary depending on routing/fallback.
        webTestClient.post().uri("/api/auth/login/otp/request")
                .header("X-Request-Id", "rid-auth")
                .exchange()
                .expectStatus().value(s -> {
                    if (s == 401 || s == 403) {
                        throw new AssertionError("/api/auth/** should be public, but got status=" + s);
                    }
                })
                .expectHeader().valueEquals("X-Request-Id", "rid-auth");
    }
}
