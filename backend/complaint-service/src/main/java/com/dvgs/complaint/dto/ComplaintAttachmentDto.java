package com.dvgs.complaint.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintAttachmentDto {
    UUID id;
    String fileName;
    String fileType;
    long fileSizeBytes;
    String storagePath;
    Instant createdAt;
}
