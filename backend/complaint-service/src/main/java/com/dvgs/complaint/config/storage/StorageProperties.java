package com.dvgs.complaint.config.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "attachments")
public class StorageProperties {

    private String basePath = "uploads";
    private long maxSizeBytes = 5 * 1024 * 1024; // 5MB default
    private String[] allowedMimeTypes = {"image/png", "image/jpeg", "application/pdf"};
    private String downloadSecret = "change-me";
    private long downloadTtlSeconds = 300;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public String[] getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(String[] allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public String getDownloadSecret() {
        return downloadSecret;
    }

    public void setDownloadSecret(String downloadSecret) {
        this.downloadSecret = downloadSecret;
    }

    public long getDownloadTtlSeconds() {
        return downloadTtlSeconds;
    }

    public void setDownloadTtlSeconds(long downloadTtlSeconds) {
        this.downloadTtlSeconds = downloadTtlSeconds;
    }
}
