package com.dvgs.scheme.attachments;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioConfigValidator {

    private final MinioProperties properties;

    @PostConstruct
    public void validate() {
        if (properties.getAccessKey() == null || properties.getAccessKey().isBlank()
                || properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException(
                    "MinIO credentials not configured. Please set MINIO_ACCESS_KEY and MINIO_SECRET_KEY environment variables.");
        }
    }
}
