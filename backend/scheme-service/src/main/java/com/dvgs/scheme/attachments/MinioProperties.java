package com.dvgs.scheme.attachments;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "attachments.minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";
    private String region = "us-east-1";
    private String bucket = "dvgs-attachments";
    private String accessKey;
    private String secretKey;
    private int presignExpiryMinutes = 15;
    private long maxSizeBytes = 5 * 1024 * 1024;
    /** comma-separated in yml; spring can bind to list if provided as array, so we parse in service */
    private String allowedExtensions = "pdf,jpg,jpeg,png";

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public int getPresignExpiryMinutes() { return presignExpiryMinutes; }
    public void setPresignExpiryMinutes(int presignExpiryMinutes) { this.presignExpiryMinutes = presignExpiryMinutes; }

    public long getMaxSizeBytes() { return maxSizeBytes; }
    public void setMaxSizeBytes(long maxSizeBytes) { this.maxSizeBytes = maxSizeBytes; }

    public String getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; }

    public List<String> allowedExtensionsList() {
        if (allowedExtensions == null || allowedExtensions.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList();
    }
}
