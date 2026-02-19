package com.dvgs.scheme.dto;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.domain.DocumentVerifiedStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public class ApplicationDtos {

    public record ApplicationDocumentRequest(
            @NotBlank @Size(max = 64) String docType,
            String fileRef
    ) {}

    public record CreateApplicationRequest(
            @NotNull Long schemeId,
            @Valid List<ApplicationDocumentRequest> documents
    ) {}

    public record CreateDraftRequest(
            @NotNull Long schemeId
    ) {}

    public record UpdateDraftDocumentsRequest(
            @Valid List<ApplicationDocumentRequest> documents
    ) {}

    public record CancelApplicationRequest(
            String reason
    ) {}

    public record ApplicationDocumentResponse(
            String docType,
            String fileRef,
            DocumentVerifiedStatus verifiedStatus,
            String remarks
    ) {}

    public record StatusHistoryResponse(
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            String changedBy,
            Instant changedAt,
            String remarks
    ) {}

    public record ApplicationResponse(
            Long id,
            Long schemeId,
            String applicantUserId,
            ApplicationStatus status,
            String assignedOfficerId,
            Instant submittedAt,
            Instant createdAt,
            Instant updatedAt,
            List<ApplicationDocumentResponse> documents,
            List<StatusHistoryResponse> statusHistory
    ) {}
}
