package com.dvgs.scheme.dto;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.domain.DocumentVerifiedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OfficerReviewDtos {

    public record AssignOfficerRequest(
            @NotBlank @Size(max = 128) String officerId
    ) {}

    public record ChangeStatusRequest(
            @NotNull ApplicationStatus toStatus,
            String remarks
    ) {}

    public record VerifyDocumentRequest(
            @NotBlank @Size(max = 64) String docType,
            @NotNull DocumentVerifiedStatus verifiedStatus,
            String remarks
    ) {}
}
