package com.dvgs.scheme.controller;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.dto.ApplicationDtos;
import com.dvgs.scheme.dto.OfficerReviewDtos;
import com.dvgs.scheme.service.OfficerReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/officer/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OFFICIAL','ADMIN','SUPER_ADMIN')")
public class OfficerReviewController {

    private final OfficerReviewService officerReviewService;

    /**
     * List applications by status. Optional schemeId filter.
     */
    @GetMapping
    public List<ApplicationDtos.ApplicationResponse> list(
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) Long schemeId
    ) {
        return officerReviewService.listByStatus(schemeId, status);
    }

    @GetMapping("/{id}")
    public ApplicationDtos.ApplicationResponse get(@PathVariable Long id) {
        return officerReviewService.getById(id);
    }

    @PostMapping("/{id}/assign")
    public ApplicationDtos.ApplicationResponse assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody OfficerReviewDtos.AssignOfficerRequest req
    ) {
        return officerReviewService.assignOfficer(id, req);
    }

    @PostMapping("/{id}/status")
    public ApplicationDtos.ApplicationResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody OfficerReviewDtos.ChangeStatusRequest req
    ) {
        return officerReviewService.changeStatus(id, req);
    }

    @PostMapping("/{id}/documents/verify")
    public ApplicationDtos.ApplicationResponse verifyDocument(
            @PathVariable Long id,
            @Valid @RequestBody OfficerReviewDtos.VerifyDocumentRequest req
    ) {
        return officerReviewService.verifyDocument(id, req);
    }
}
