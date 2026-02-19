package com.dvgs.scheme.service;

import com.dvgs.scheme.domain.*;
import com.dvgs.scheme.dto.ApplicationDtos;
import com.dvgs.scheme.dto.OfficerReviewDtos;
import com.dvgs.scheme.event.SchemeEvent;
import com.dvgs.scheme.event.SchemeEventPublisher;
import com.dvgs.scheme.exception.MissingRequiredDocumentsException;
import com.dvgs.scheme.repository.SchemeApplicationRepository;
import com.dvgs.scheme.repository.SchemeDocumentRequirementRepository;
import com.dvgs.scheme.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfficerReviewService {

    private final SchemeApplicationRepository applicationRepository;
    private final SchemeDocumentRequirementRepository documentRequirementRepository;
    private final AuditService auditService;
    private final SchemeEventPublisher schemeEventPublisher;

    @Transactional(readOnly = true)
    public List<ApplicationDtos.ApplicationResponse> listByStatus(Long schemeId, ApplicationStatus status) {
        List<SchemeApplication> apps;
        if (schemeId != null) {
            apps = applicationRepository.findByScheme_IdAndStatusOrderByUpdatedAtDesc(schemeId, status);
        } else {
            apps = applicationRepository.findByStatusOrderByUpdatedAtDesc(status);
        }
        return apps.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponse getById(Long id) {
        SchemeApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));
        return toResponse(app);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse assignOfficer(Long applicationId, OfficerReviewDtos.AssignOfficerRequest req) {
        SchemeApplication app = getApp(applicationId);
        app.setAssignedOfficerId(req.officerId());
        SchemeApplication saved = applicationRepository.save(app);
        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "ASSIGN_OFFICER",
                "Assigned officerId=" + req.officerId(),
                null
        );

        schemeEventPublisher.publish(SchemeEvent.builder()
                .applicationId(saved.getId())
                .schemeId(saved.getScheme().getId())
                .applicantUserId(saved.getApplicantUserId())
                .assignedOfficerId(saved.getAssignedOfficerId())
                .status(saved.getStatus())
                .type("OFFICER_ASSIGNED")
                .timestamp(Instant.now())
                .description("Officer assigned")
                .build());

        return toResponse(saved);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse changeStatus(Long applicationId, OfficerReviewDtos.ChangeStatusRequest req) {
        SchemeApplication app = getApp(applicationId);

        ApplicationStatus from = app.getStatus();
        ApplicationStatus to = req.toStatus();

        if (!isAllowedTransition(from, to)) {
            throw new IllegalArgumentException("Invalid status transition: " + from + " -> " + to);
        }

        // Before approval, ensure all required docs are present (fileRef not blank)
        if (to == ApplicationStatus.APPROVED) {
            validateRequiredDocumentsOrThrow(app);
        }

        String actor = requireUser();

        app.setStatus(to);
        if (to == ApplicationStatus.SUBMITTED && app.getSubmittedAt() == null) {
            app.setSubmittedAt(Instant.now());
        }

        SchemeApplicationStatusHistory h = new SchemeApplicationStatusHistory();
        h.setApplication(app);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedBy(actor);
        h.setChangedAt(Instant.now());
        h.setRemarks(req.remarks());
        app.getStatusHistory().add(h);

        SchemeApplication saved = applicationRepository.save(app);
        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "STATUS_CHANGE",
                "Status changed: " + from + " -> " + to,
                null
        );

        String eventType = switch (to) {
            case NEED_MORE_INFO -> "APPLICATION_NEED_MORE_INFO";
            case APPROVED -> "APPLICATION_APPROVED";
            case REJECTED -> "APPLICATION_REJECTED";
            case BENEFIT_DISBURSED -> "BENEFIT_DISBURSED";
            default -> "APPLICATION_STATUS_CHANGED";
        };

        schemeEventPublisher.publish(SchemeEvent.builder()
                .applicationId(saved.getId())
                .schemeId(saved.getScheme().getId())
                .applicantUserId(saved.getApplicantUserId())
                .assignedOfficerId(saved.getAssignedOfficerId())
                .status(saved.getStatus())
                .type(eventType)
                .timestamp(Instant.now())
                .description(req.remarks() != null ? req.remarks() : ("Status changed: " + from + " -> " + to))
                .build());

        return toResponse(saved);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse verifyDocument(Long applicationId, OfficerReviewDtos.VerifyDocumentRequest req) {
        SchemeApplication app = getApp(applicationId);

        SchemeApplicationDocument doc = app.getDocuments().stream()
                .filter(d -> d.getDocType().equalsIgnoreCase(req.docType()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document not found for applicationId=" + applicationId + ", docType=" + req.docType()));

        doc.setVerifiedStatus(req.verifiedStatus());
        doc.setRemarks(req.remarks());

        SchemeApplication saved = applicationRepository.save(app);
        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "VERIFY_DOCUMENT",
                "Document " + doc.getDocType() + " -> " + doc.getVerifiedStatus(),
                null
        );

        return toResponse(saved);
    }

    private void validateRequiredDocumentsOrThrow(SchemeApplication app) {
        Long schemeId = app.getScheme().getId();
        var required = documentRequirementRepository.findRequiredDocTypes(schemeId);
        if (required == null || required.isEmpty()) {
            return;
        }

        var provided = app.getDocuments().stream()
                .filter(d -> d.getFileRef() != null && !d.getFileRef().isBlank())
                .map(d -> d.getDocType() != null ? d.getDocType().trim().toLowerCase() : "")
                .filter(s -> !s.isBlank())
                .collect(java.util.stream.Collectors.toSet());

        var missing = required.stream()
                .map(s -> s != null ? s.trim().toLowerCase() : "")
                .filter(s -> !s.isBlank())
                .filter(req -> !provided.contains(req))
                .toList();

        if (!missing.isEmpty()) {
            throw new MissingRequiredDocumentsException(schemeId, missing);
        }
    }

    private SchemeApplication getApp(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));
    }

    private boolean isAllowedTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return switch (from) {
            case SUBMITTED -> to == ApplicationStatus.UNDER_REVIEW || to == ApplicationStatus.CANCELLED;
            case UNDER_REVIEW -> to == ApplicationStatus.NEED_MORE_INFO
                    || to == ApplicationStatus.VERIFIED
                    || to == ApplicationStatus.APPROVED
                    || to == ApplicationStatus.REJECTED;
            case NEED_MORE_INFO -> to == ApplicationStatus.UNDER_REVIEW || to == ApplicationStatus.REJECTED;
            case VERIFIED -> to == ApplicationStatus.APPROVED || to == ApplicationStatus.REJECTED;
            case APPROVED -> to == ApplicationStatus.BENEFIT_DISBURSED;
            case REJECTED, CANCELLED, BENEFIT_DISBURSED, DRAFT -> false;
        };
    }

    private String requireUser() {
        String userId = SecurityUtils.currentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Missing authenticated user");
        }
        return userId;
    }

    private ApplicationDtos.ApplicationResponse toResponse(SchemeApplication app) {
        // reuse mapping logic similar to SchemeApplicationService, avoid circular dep
        List<ApplicationDtos.ApplicationDocumentResponse> docs = new ArrayList<>();
        if (app.getDocuments() != null) {
            for (SchemeApplicationDocument d : app.getDocuments()) {
                docs.add(new ApplicationDtos.ApplicationDocumentResponse(
                        d.getDocType(),
                        d.getFileRef(),
                        d.getVerifiedStatus(),
                        d.getRemarks()
                ));
            }
        }

        List<ApplicationDtos.StatusHistoryResponse> history = new ArrayList<>();
        if (app.getStatusHistory() != null) {
            for (SchemeApplicationStatusHistory h : app.getStatusHistory()) {
                history.add(new ApplicationDtos.StatusHistoryResponse(
                        h.getFromStatus(),
                        h.getToStatus(),
                        h.getChangedBy(),
                        h.getChangedAt(),
                        h.getRemarks()
                ));
            }
        }

        return new ApplicationDtos.ApplicationResponse(
                app.getId(),
                app.getScheme().getId(),
                app.getApplicantUserId(),
                app.getStatus(),
                app.getAssignedOfficerId(),
                app.getSubmittedAt(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                docs,
                history
        );
    }
}
