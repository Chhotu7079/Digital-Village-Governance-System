package com.dvgs.scheme.dto;

import com.dvgs.scheme.domain.SchemeStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SchemeDtos {

    public record DocumentRequirementDto(
            @NotBlank @Size(max = 64) String docType,
            @NotNull Boolean required
    ) {}

    public record CreateSchemeRequest(
            @NotBlank @Size(max = 64) String schemeCode,
            @NotBlank String name,
            String description,
            String department,
            String benefitType,
            String benefitDetails,
            SchemeStatus status,
            LocalDate startDate,
            LocalDate endDate,
            @Valid List<DocumentRequirementDto> documentRequirements
    ) {}

    public record UpdateSchemeRequest(
            @NotBlank String name,
            String description,
            String department,
            String benefitType,
            String benefitDetails,
            SchemeStatus status,
            LocalDate startDate,
            LocalDate endDate,
            @Valid List<DocumentRequirementDto> documentRequirements
    ) {}

    public record SchemeResponse(
            Long id,
            String schemeCode,
            String name,
            String description,
            String department,
            String benefitType,
            String benefitDetails,
            SchemeStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant createdAt,
            Instant updatedAt,
            List<DocumentRequirementDto> documentRequirements
    ) {}
}
