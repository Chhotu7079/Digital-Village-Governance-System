package com.dvgs.ration;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RationServiceSmokeTest {

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
        registry.add("ration.external.base-url", () -> mockWebServer.url("/").toString());
        registry.add("spring.security.oauth2.resourceserver.jwt.secret", () -> "test-secret");
        registry.add("auth.jwt.issuer", () -> "dvgs-auth");
        registry.add("ration.cache.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "NONE");
    }

    @Test
    void citizenGetsOnlyExistsTypeStatusAndCount() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          \"cardNo\": \"BR-RC-0003\",
                          \"cardType\": \"PHH\",
                          \"status\": \"ACTIVE\",
                          \"location\": {\"district\":\"Nalanda\",\"anchal\":\"Biharsharif\",\"mauza\":\"Mauza-021\"},
                          \"members\": [
                            {\"name\":\"A\",\"relation\":\"HEAD\",\"dob\":\"1980-01-01\"},
                            {\"name\":\"B\",\"relation\":\"SPOUSE\",\"dob\":\"1981-01-01\"},
                            {\"name\":\"C\",\"relation\":\"CHILD\",\"dob\":\"2010-01-01\"}
                          ]
                        }
                        """));

        // Single request: fetch body as string then assert JSON
        String body = securedClient()
                .mutateWith(mockJwt().authorities(() -> "ROLE_CITIZEN").jwt(jwt -> jwt.claim("iss", "dvgs-auth")))
                .get().uri("/api/ration/cards/BR-RC-0003")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        if (body == null || !body.contains("\"exists\"")) {
            throw new AssertionError("Unexpected response body: " + body);
        }

        try {
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            org.junit.jupiter.api.Assertions.assertTrue(node.get("exists").asBoolean());
            org.junit.jupiter.api.Assertions.assertEquals("BR-RC-0003", node.get("cardNo").asText());
            org.junit.jupiter.api.Assertions.assertEquals("PHH", node.get("cardType").asText());
            org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", node.get("status").asText());
            org.junit.jupiter.api.Assertions.assertEquals(3, node.get("memberCount").asInt());
            org.junit.jupiter.api.Assertions.assertTrue(node.get("members").isArray());
            org.junit.jupiter.api.Assertions.assertEquals(0, node.get("members").size());
            org.junit.jupiter.api.Assertions.assertTrue(node.get("location").isNull());
        } catch (Exception e) {
            throw new AssertionError("Failed to parse/assert JSON body: " + body, e);
        }
    }
}
