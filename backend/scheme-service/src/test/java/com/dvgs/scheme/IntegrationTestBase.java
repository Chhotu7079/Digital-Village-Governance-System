package com.dvgs.scheme;

import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES;

    static {
        PostgreSQLContainer<?> container;
        try {
            container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("dvgs_scheme")
                    .withUsername("dvgs")
                    .withPassword("dvgs");
        } catch (Throwable t) {
            container = null;
        }
        POSTGRES = container;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Integration tests are gated by @EnabledIfSystemProperty(it.docker=true) on test classes.
        // When enabled, Docker must be available for Testcontainers.

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Flyway should run against the container DB
        registry.add("spring.flyway.enabled", () -> true);
        // allow Hibernate validate against migrated schema
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // Avoid failing on JWT issuer during tests; security can be overridden per-test with MockMvc security.
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://test-issuer");

        // Disable kafka auto startup issues if any consumer beans exist (defensive)
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:0");

        // MinIO env placeholders so app context can start (tests won't call S3 unless needed)
        registry.add("attachments.minio.endpoint", () -> "http://localhost:9000");
        registry.add("attachments.minio.region", () -> "us-east-1");
        registry.add("attachments.minio.bucket", () -> "dvgs-attachments");
        registry.add("attachments.minio.access-key", () -> "test");
        registry.add("attachments.minio.secret-key", () -> "test");
    }
}
