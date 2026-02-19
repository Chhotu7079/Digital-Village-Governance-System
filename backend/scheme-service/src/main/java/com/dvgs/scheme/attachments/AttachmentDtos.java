package com.dvgs.scheme.attachments;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class AttachmentDtos {

    public record PresignUploadRequest(
            @NotNull Long schemeId,
            @NotBlank @Size(max = 64) String docType,
            @NotBlank @Size(max = 256) String fileName,
            @NotBlank @Size(max = 128) String contentType,
            @Min(1) long sizeBytes
    ) {}

    public record PresignUploadResponse(
            String fileRef,
            String uploadUrl,
            Instant expiresAt
    ) {}

    public record PresignDownloadRequest(
            @NotBlank @Size(max = 1024) String fileRef
    ) {}

    public record PresignDownloadResponse(
            String fileRef,
            String downloadUrl,
            Instant expiresAt
    ) {}
}
