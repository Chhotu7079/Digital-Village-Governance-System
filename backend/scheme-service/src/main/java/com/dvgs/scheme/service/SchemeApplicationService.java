package com.dvgs.scheme.service;

import com.dvgs.scheme.attachments.AttachmentIntegrityService;
import com.dvgs.scheme.domain.*;
import com.dvgs.scheme.dto.ApplicationDtos;
import com.dvgs.scheme.event.SchemeEvent;
import com.dvgs.scheme.event.SchemeEventPublisher;
import com.dvgs.scheme.exception.MissingRequiredDocumentsException;
import com.dvgs.scheme.exception.MissingUploadedDocumentsException;
import com.dvgs.scheme.exception.SchemeArchivedException;
import com.dvgs.scheme.repository.SchemeApplicationRepository;
import com.dvgs.scheme.repository.SchemeDocumentRequirementRepository;
import com.dvgs.scheme.config.SchemeApplicationProperties;
import com.dvgs.scheme.exception.ActiveApplicationExistsException;
import com.dvgs.scheme.exception.ReapplyCooldownException;
import com.dvgs.scheme.repository.SchemeRepository;
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
public class SchemeApplicationService {

    private final SchemeRepository schemeRepository;
    private final SchemeApplicationRepository applicationRepository;
    private final SchemeDocumentRequirementRepository documentRequirementRepository;
    private final SchemeApplicationProperties applicationProperties;
    private final AuditService auditService;
    private final SchemeEventPublisher schemeEventPublisher;
    private final AttachmentIntegrityService attachmentIntegrityService;

    /**
     * Backwards-compatible: directly create and submit an application in one call.
     * Prefer using draft -> submit endpoints instead.
     */
    @Transactional
    public ApplicationDtos.ApplicationResponse create(ApplicationDtos.CreateApplicationRequest req) {
        String userId = requireUser();
        Scheme scheme = schemeRepository.findById(req.schemeId())
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + req.schemeId()));

        enforceSchemeNotArchived(scheme);
        enforceSingleActiveApplication(userId, scheme.getId());
        enforceReapplyCooldownIfNeeded(userId, scheme.getId());

        SchemeApplication app = new SchemeApplication();
        app.setScheme(scheme);
        app.setApplicantUserId(userId);
        app.setStatus(ApplicationStatus.DRAFT);

        if (req.documents() != null) {
            upsertDocuments(app, req.documents());
        }

        SchemeApplication saved = applicationRepository.save(app);
        return submitInternal(saved.getId(), userId);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse createDraft(ApplicationDtos.CreateDraftRequest req) {
        String userId = requireUser();

        Scheme scheme = schemeRepository.findById(req.schemeId())
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + req.schemeId()));

        enforceSingleActiveApplication(userId, scheme.getId());
        enforceReapplyCooldownIfNeeded(userId, scheme.getId());

        // Prevent multiple simultaneous drafts per (userId, schemeId)
        var existingDraft = applicationRepository
                .findFirstByApplicantUserIdAndScheme_IdAndStatusOrderByCreatedAtDesc(userId, scheme.getId(), ApplicationStatus.DRAFT);
        if (existingDraft.isPresent()) {
            auditService.log(
                    "APPLICATION",
                    existingDraft.get().getId().toString(),
                    "DRAFT_REUSE",
                    "Existing draft reused for schemeId=" + scheme.getId(),
                    null
            );
            return toResponse(existingDraft.get());
        }

        SchemeApplication app = new SchemeApplication();
        app.setScheme(scheme);
        app.setApplicantUserId(userId);
        app.setStatus(ApplicationStatus.DRAFT);

        SchemeApplication saved = applicationRepository.save(app);

        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "DRAFT_CREATE",
                "Draft created for schemeId=" + scheme.getId(),
                null
        );

        return toResponse(saved);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse updateDraftDocuments(Long applicationId, ApplicationDtos.UpdateDraftDocumentsRequest req) {
        String userId = requireUser();

        SchemeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + applicationId));

        if (!userId.equals(app.getApplicantUserId()) || app.getStatus() != ApplicationStatus.DRAFT) {
            throw new EntityNotFoundException("Application not found: " + applicationId);
        }

        upsertDocuments(app, req.documents());
        SchemeApplication saved = applicationRepository.save(app);

        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "DRAFT_UPDATE",
                "Draft documents updated",
                null
        );

        return toResponse(saved);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse submitDraft(Long applicationId) {
        String userId = requireUser();
        return submitInternal(applicationId, userId);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse cancelMyApplication(Long applicationId, ApplicationDtos.CancelApplicationRequest req) {
        String userId = requireUser();

        SchemeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + applicationId));

        if (!userId.equals(app.getApplicantUserId())) {
            throw new EntityNotFoundException("Application not found: " + applicationId);
        }

        ApplicationStatus from = app.getStatus();
        if (from != ApplicationStatus.DRAFT && from != ApplicationStatus.SUBMITTED && from != ApplicationStatus.NEED_MORE_INFO) {
            throw new IllegalArgumentException("Only DRAFT/SUBMITTED/NEED_MORE_INFO applications can be cancelled");
        }

        app.setStatus(ApplicationStatus.CANCELLED);

        SchemeApplicationStatusHistory h = new SchemeApplicationStatusHistory();
        h.setApplication(app);
        h.setFromStatus(from);
        h.setToStatus(ApplicationStatus.CANCELLED);
        h.setChangedBy(userId);
        h.setChangedAt(Instant.now());
        h.setRemarks(req != null ? req.reason() : null);
        app.getStatusHistory().add(h);

        SchemeApplication saved = applicationRepository.save(app);

        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "CANCEL",
                "Application cancelled (from=" + from + ")",
                null
        );

        // Optional: publish event for notification/audit downstream
        schemeEventPublisher.publish(SchemeEvent.builder()
                .applicationId(saved.getId())
                .schemeId(saved.getScheme().getId())
                .applicantUserId(saved.getApplicantUserId())
                .assignedOfficerId(saved.getAssignedOfficerId())
                .status(saved.getStatus())
                .type("APPLICATION_CANCELLED")
                .timestamp(Instant.now())
                .description(req != null ? req.reason() : "Application cancelled")
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDtos.ApplicationResponse> listMine() {
        String userId = requireUser();
        return applicationRepository.findByApplicantUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponse getMine(Long id) {
        String userId = requireUser();
        SchemeApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));

        if (!userId.equals(app.getApplicantUserId())) {
            // avoid leaking existence
            throw new EntityNotFoundException("Application not found: " + id);
        }
        return toResponse(app);
    }

    private void enforceSingleActiveApplication(String userId, Long schemeId) {
        // DRAFT is handled separately via draft reuse logic.
        var activeStatuses = java.util.List.of(
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_REVIEW,
                ApplicationStatus.NEED_MORE_INFO,
                ApplicationStatus.VERIFIED,
                ApplicationStatus.APPROVED
        );

        applicationRepository.findFirstByApplicantUserIdAndScheme_IdAndStatusInOrderByCreatedAtDesc(userId, schemeId, activeStatuses)
                .ifPresent(existing -> {
                    throw new ActiveApplicationExistsException(schemeId, existing.getId(), existing.getStatus());
                });
    }

    private void enforceReapplyCooldownIfNeeded(String userId, Long schemeId) {
        int cooldownDays = Math.max(applicationProperties.getReapplyCooldownDays(), 0);
        if (cooldownDays == 0) {
            return;
        }

        var lastOpt = applicationRepository.findFirstByApplicantUserIdAndScheme_IdOrderByCreatedAtDesc(userId, schemeId);
        if (lastOpt.isEmpty()) {
            return;
        }

        SchemeApplication last = lastOpt.get();
        if (last.getStatus() != ApplicationStatus.REJECTED) {
            return;
        }

        Instant since = last.getUpdatedAt() != null ? last.getUpdatedAt() : last.getCreatedAt();
        if (since == null) {
            return;
        }

        long daysSince = java.time.Duration.between(since, Instant.now()).toDays();
        long remaining = cooldownDays - daysSince;
        if (remaining > 0) {
            throw new ReapplyCooldownException(schemeId, remaining);
        }
    }

    private ApplicationDtos.ApplicationResponse submitInternal(Long applicationId, String userId) {
        SchemeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + applicationId));

        if (!userId.equals(app.getApplicantUserId())) {
            throw new EntityNotFoundException("Application not found: " + applicationId);
        }

        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT applications can be submitted");
        }

        validateRequiredDocumentsOrThrow(app.getScheme().getId(), app);
        validateUploadedObjectsExistOrThrow(app);

        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setSubmittedAt(Instant.now());

        SchemeApplicationStatusHistory h = new SchemeApplicationStatusHistory();
        h.setApplication(app);
        h.setFromStatus(ApplicationStatus.DRAFT);
        h.setToStatus(ApplicationStatus.SUBMITTED);
        h.setChangedBy(userId);
        h.setChangedAt(Instant.now());
        h.setRemarks("Application submitted");
        app.getStatusHistory().add(h);

        SchemeApplication saved = applicationRepository.save(app);

        auditService.log(
                "APPLICATION",
                saved.getId().toString(),
                "SUBMIT",
                "Application submitted for schemeId=" + saved.getScheme().getId(),
                null
        );

        schemeEventPublisher.publish(SchemeEvent.builder()
                .applicationId(saved.getId())
                .schemeId(saved.getScheme().getId())
                .applicantUserId(saved.getApplicantUserId())
                .assignedOfficerId(saved.getAssignedOfficerId())
                .status(saved.getStatus())
                .type("APPLICATION_SUBMITTED")
                .timestamp(Instant.now())
                .description("Scheme application submitted")
                .build());

        return toResponse(saved);
    }

    private void upsertDocuments(SchemeApplication app, List<ApplicationDtos.ApplicationDocumentRequest> docs) {
        if (docs == null) {
            return;
        }
        // Replace-all strategy for draft updates
        app.getDocuments().clear();
        for (ApplicationDtos.ApplicationDocumentRequest d : docs) {
            SchemeApplicationDocument doc = new SchemeApplicationDocument();
            doc.setApplication(app);
            doc.setDocType(d.docType());
            doc.setFileRef(d.fileRef());
            doc.setVerifiedStatus(DocumentVerifiedStatus.PENDING);
            app.getDocuments().add(doc);
        }
    }

    private void validateRequiredDocumentsOrThrow(Long schemeId, SchemeApplication app) {
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

    private void enforceSchemeNotArchived(Scheme scheme) {
        if (scheme != null && scheme.getStatus() == SchemeStatus.ARCHIVED) {
            throw new SchemeArchivedException(scheme.getId());
        }
    }

    private void validateUploadedObjectsExistOrThrow(SchemeApplication app) {
        if (app.getDocuments() == null || app.getDocuments().isEmpty()) {
            return;
        }

        List<String> missing = new ArrayList<>();
        for (SchemeApplicationDocument d : app.getDocuments()) {
            if (d.getFileRef() == null || d.getFileRef().isBlank()) {
                continue;
            }
            boolean exists = attachmentIntegrityService.exists(d.getFileRef());
            if (!exists) {
                missing.add(d.getDocType());
            }
        }

        if (!missing.isEmpty()) {
            throw new MissingUploadedDocumentsException(app.getId(), missing);
        }
    }

    private String requireUser() {
        String userId = SecurityUtils.currentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Missing authenticated user");
        }
        return userId;
    }

    private ApplicationDtos.ApplicationResponse toResponse(SchemeApplication app) {
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
