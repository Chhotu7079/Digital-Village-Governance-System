package com.dvgs.complaint.storage;

import com.dvgs.complaint.config.storage.StorageProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AttachmentDownloadTokenService {

    private final StorageProperties properties;

    public AttachmentDownloadTokenService(StorageProperties properties) {
        this.properties = properties;
    }

    public String issueToken(UUID attachmentId) {
        long expiresAt = Instant.now().plusSeconds(properties.getDownloadTtlSeconds()).getEpochSecond();
        String payload = attachmentId + ":" + expiresAt;
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token, UUID attachmentId) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return false;
            }
            UUID tokenAttachment = UUID.fromString(parts[0]);
            long expires = Long.parseLong(parts[1]);
            String signature = parts[2];
            if (!tokenAttachment.equals(attachmentId)) {
                return false;
            }
            if (Instant.now().getEpochSecond() > expires) {
                return false;
            }
            String expected = sign(parts[0] + ":" + parts[1]);
            return expected.equals(signature);
        } catch (Exception ex) {
            return false;
        }
    }

    private String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(payload.getBytes(StandardCharsets.UTF_8));
            digest.update(properties.getDownloadSecret().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }
}
