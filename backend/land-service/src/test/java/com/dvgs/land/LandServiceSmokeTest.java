package com.dvgs.land;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LandServiceSmokeTest {

    static MockWebServer mockWebServer;

    @Autowired
    ApplicationContext applicationContext;

    private WebTestClient securedClient() {
        return WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("land.external.base-url", () -> mockWebServer.url("/").toString());
        registry.add("spring.security.oauth2.resourceserver.jwt.secret", () -> "test-secret");
        registry.add("auth.jwt.issuer", () -> "dvgs-auth");
        registry.add("land.cache.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "NONE");
    }

    @Test
    void citizenGetsOnlyExistsIdentifiersAreaAndOwnerCount() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          \"district\": \"Patna\",
                          \"anchal\": \"Danapur\",
                          \"mauza\": \"Mauza-001\",
                          \"khataNo\": \"15\",
                          \"khesraNo\": \"221\",
                          \"area\": 0.52,
                          \"unit\": \"ACRE\",
                          \"jamabandiNo\": \"JMB-1001\",
                          \"owners\": [
                            {\"name\":\"Ram Kumar\",\"sharePercent\":50},
                            {\"name\":\"Sita Devi\",\"sharePercent\":50}
                          ]
                        }
                        """));

        String body = securedClient()
                .mutateWith(mockJwt().authorities(() -> "ROLE_CITIZEN").jwt(jwt -> jwt.claim("iss", "dvgs-auth")))
                .get().uri(uriBuilder -> uriBuilder
                        .path("/api/land/records/search")
                        .queryParam("district", "Patna")
                        .queryParam("anchal", "Danapur")
                        .queryParam("mauza", "Mauza-001")
                        .queryParam("khataNo", "15")
                        .queryParam("khesraNo", "221")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        if (body == null || !body.contains("\"exists\"")) {
            throw new AssertionError("Unexpected response body: " + body);
        }

        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            org.junit.jupiter.api.Assertions.assertTrue(node.get("exists").asBoolean());
            org.junit.jupiter.api.Assertions.assertEquals("Patna", node.get("district").asText());
            org.junit.jupiter.api.Assertions.assertEquals("Danapur", node.get("anchal").asText());
            org.junit.jupiter.api.Assertions.assertEquals("Mauza-001", node.get("mauza").asText());
            org.junit.jupiter.api.Assertions.assertEquals("15", node.get("khataNo").asText());
            org.junit.jupiter.api.Assertions.assertEquals("221", node.get("khesraNo").asText());
            org.junit.jupiter.api.Assertions.assertEquals(0.52, node.get("area").asDouble(), 0.0001);
            org.junit.jupiter.api.Assertions.assertEquals("ACRE", node.get("unit").asText());
            org.junit.jupiter.api.Assertions.assertEquals(2, node.get("ownerCount").asInt());

            // Citizen should not see these
            org.junit.jupiter.api.Assertions.assertTrue(node.get("owners").isArray());
            org.junit.jupiter.api.Assertions.assertEquals(0, node.get("owners").size());
            org.junit.jupiter.api.Assertions.assertTrue(node.get("jamabandiNo").isNull());
        } catch (Exception e) {
            throw new AssertionError("Failed to parse/assert JSON body: " + body, e);
        }
    }
}
