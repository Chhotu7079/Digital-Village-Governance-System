package com.dvgs.scheme.attachments;

import com.dvgs.scheme.security.SecurityUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final String FILE_REF_PREFIX = "minio://";

    private final MinioProperties properties;
    private final S3Presigner presigner;
    private final AttachmentAccessService accessService;

    public AttachmentDtos.PresignUploadResponse presignUpload(AttachmentDtos.PresignUploadRequest req) {
        String userId = SecurityUtils.currentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Missing authenticated user");
        }

        if (req.sizeBytes() > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("File too large. Max size bytes=" + properties.getMaxSizeBytes());
        }

        String ext = extension(req.fileName());
        Set<String> allowed = Set.copyOf(properties.allowedExtensionsList());
        if (!allowed.isEmpty() && (ext == null || !allowed.contains(ext))) {
            throw new IllegalArgumentException("File extension not allowed: " + ext);
        }

        String safeDocType = req.docType().trim().toUpperCase(Locale.ROOT);
        String objectKey = "scheme/" + req.schemeId()
                + "/user/" + userId
                + "/" + safeDocType
                + "/" + UUID.randomUUID() + (ext != null ? ("." + ext) : "");

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(req.contentType())
                .build();

        Instant expiresAt = Instant.now().plus(properties.getPresignExpiryMinutes(), ChronoUnit.MINUTES);

        PresignedPutObjectRequest presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(properties.getPresignExpiryMinutes()))
                .putObjectRequest(putReq)
                .build());

        String fileRef = "minio://" + properties.getBucket() + "/" + objectKey;
        return new AttachmentDtos.PresignUploadResponse(fileRef, presigned.url().toString(), expiresAt);
    }

    public AttachmentDtos.PresignDownloadResponse presignDownload(AttachmentDtos.PresignDownloadRequest req) {
        String userId = SecurityUtils.currentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Missing authenticated user");
        }

        String fileRef = req.fileRef();
        if (fileRef == null || fileRef.isBlank() || !fileRef.startsWith(FILE_REF_PREFIX)) {
            throw new IllegalArgumentException("Invalid fileRef");
        }

        // Expected format: minio://bucket/objectKey
        String withoutPrefix = fileRef.substring(FILE_REF_PREFIX.length());
        int slash = withoutPrefix.indexOf('/');
        if (slash <= 0 || slash == withoutPrefix.length() - 1) {
            throw new IllegalArgumentException("Invalid fileRef");
        }
        String bucket = withoutPrefix.substring(0, slash);
        String key = withoutPrefix.substring(slash + 1);

        if (!bucket.equals(properties.getBucket())) {
            throw new IllegalArgumentException("Invalid bucket in fileRef");
        }

        // DB-backed access validation (citizen owns the application document; officials assigned; admins allowed)
        accessService.validateCanDownload(fileRef);

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        Instant expiresAt = Instant.now().plus(properties.getPresignExpiryMinutes(), ChronoUnit.MINUTES);

        PresignedGetObjectRequest presigned = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(properties.getPresignExpiryMinutes()))
                .getObjectRequest(getReq)
                .build());

        return new AttachmentDtos.PresignDownloadResponse(fileRef, presigned.url().toString(), expiresAt);
    }

    private String extension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String name = fileName.trim();
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return null;
        }
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}
