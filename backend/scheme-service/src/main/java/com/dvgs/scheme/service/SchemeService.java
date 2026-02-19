package com.dvgs.scheme.service;

import com.dvgs.scheme.domain.Scheme;
import com.dvgs.scheme.domain.SchemeDocumentRequirement;
import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.SchemeDtos;
import com.dvgs.scheme.repository.SchemeRepository;
import com.dvgs.scheme.repository.SchemeSpecifications;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final AuditService auditService;

    @Transactional
    public SchemeDtos.SchemeResponse create(SchemeDtos.CreateSchemeRequest req) {
        if (schemeRepository.existsBySchemeCode(req.schemeCode())) {
            throw new IllegalArgumentException("schemeCode already exists: " + req.schemeCode());
        }

        Scheme scheme = new Scheme();
        scheme.setSchemeCode(req.schemeCode());
        applyCommonFields(scheme, req.name(), req.description(), req.department(), req.benefitType(), req.benefitDetails(),
                req.status() != null ? req.status() : SchemeStatus.DRAFT,
                req.startDate(), req.endDate());

        replaceDocumentRequirements(scheme, req.documentRequirements());

        try {
            Scheme saved = schemeRepository.save(scheme);
            auditService.log(
                    "SCHEME",
                    saved.getId().toString(),
                    "CREATE",
                    "Scheme created: " + saved.getSchemeCode(),
                    null
            );
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Invalid scheme data (constraint violation)", e);
        }
    }

    @Transactional
    public SchemeDtos.SchemeResponse update(Long id, SchemeDtos.UpdateSchemeRequest req) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + id));

        applyCommonFields(scheme, req.name(), req.description(), req.department(), req.benefitType(), req.benefitDetails(),
                req.status() != null ? req.status() : scheme.getStatus(),
                req.startDate(), req.endDate());

        if (req.documentRequirements() != null) {
            replaceDocumentRequirements(scheme, req.documentRequirements());
        }

        Scheme saved = schemeRepository.save(scheme);
        auditService.log(
                "SCHEME",
                saved.getId().toString(),
                "UPDATE",
                "Scheme updated: " + saved.getSchemeCode(),
                null
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SchemeDtos.SchemeResponse getById(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + id));
        return toResponse(scheme);
    }

    @Transactional(readOnly = true)
    public List<SchemeDtos.SchemeResponse> listAll() {
        return schemeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SchemeDtos.SchemeResponse> search(
            SchemeStatus status,
            String department,
            String q,
            boolean includeArchived,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        SchemeStatus effectiveStatus = status;
        if (!includeArchived && effectiveStatus == null) {
            // Default behavior: hide archived schemes unless explicitly requested by admin.
            // (Most clients call list without status.)
            // We'll exclude ARCHIVED via specification.
        }

        var spec = SchemeSpecifications.statusEquals(effectiveStatus)
                .and(SchemeSpecifications.departmentEquals(department))
                .and(SchemeSpecifications.queryLike(q))
                .and(SchemeSpecifications.excludeArchivedUnless(includeArchived));

        return schemeRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(safePage, safeSize))
                .map(this::toResponse);
    }

    @Transactional
    public SchemeDtos.SchemeResponse archive(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + id));

        if (scheme.getStatus() != SchemeStatus.ARCHIVED) {
            scheme.setStatus(SchemeStatus.ARCHIVED);
        }

        Scheme saved = schemeRepository.save(scheme);
        auditService.log("SCHEME", saved.getId().toString(), "ARCHIVE", "Scheme archived", null);
        return toResponse(saved);
    }

    @Transactional
    public SchemeDtos.SchemeResponse unarchive(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + id));

        if (scheme.getStatus() == SchemeStatus.ARCHIVED) {
            scheme.setStatus(SchemeStatus.ACTIVE);
        }

        Scheme saved = schemeRepository.save(scheme);
        auditService.log("SCHEME", saved.getId().toString(), "UNARCHIVE", "Scheme unarchived", null);
        return toResponse(saved);
    }

    private void applyCommonFields(
            Scheme scheme,
            String name,
            String description,
            String department,
            String benefitType,
            String benefitDetails,
            SchemeStatus status,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        scheme.setName(name);
        scheme.setDescription(description);
        scheme.setDepartment(department);
        scheme.setBenefitType(benefitType);
        scheme.setBenefitDetails(benefitDetails);
        scheme.setStatus(status);
        scheme.setStartDate(startDate);
        scheme.setEndDate(endDate);
    }

    private void replaceDocumentRequirements(Scheme scheme, List<SchemeDtos.DocumentRequirementDto> reqs) {
        scheme.getDocumentRequirements().clear();
        if (reqs == null) {
            return;
        }
        for (SchemeDtos.DocumentRequirementDto dto : reqs) {
            SchemeDocumentRequirement r = new SchemeDocumentRequirement();
            r.setScheme(scheme);
            r.setDocType(dto.docType());
            r.setRequired(Boolean.TRUE.equals(dto.required()));
            scheme.getDocumentRequirements().add(r);
        }
    }

    private SchemeDtos.SchemeResponse toResponse(Scheme scheme) {
        List<SchemeDtos.DocumentRequirementDto> docs = new ArrayList<>();
        if (scheme.getDocumentRequirements() != null) {
            for (SchemeDocumentRequirement r : scheme.getDocumentRequirements()) {
                docs.add(new SchemeDtos.DocumentRequirementDto(r.getDocType(), r.isRequired()));
            }
        }

        return new SchemeDtos.SchemeResponse(
                scheme.getId(),
                scheme.getSchemeCode(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getDepartment(),
                scheme.getBenefitType(),
                scheme.getBenefitDetails(),
                scheme.getStatus(),
                scheme.getStartDate(),
                scheme.getEndDate(),
                scheme.getCreatedAt(),
                scheme.getUpdatedAt(),
                docs
        );
    }
}
