package com.dvgs.scheme.controller;

import com.dvgs.scheme.dto.ApplicationDtos;
import com.dvgs.scheme.service.SchemeApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final SchemeApplicationService schemeApplicationService;

    /**
     * Backwards-compatible: create + submit in one call.
     * Prefer draft flow endpoints below.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse apply(@Valid @RequestBody ApplicationDtos.CreateApplicationRequest req) {
        return schemeApplicationService.create(req);
    }

    @PostMapping("/draft")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse createDraft(@Valid @RequestBody ApplicationDtos.CreateDraftRequest req) {
        return schemeApplicationService.createDraft(req);
    }

    @PatchMapping("/my/{id}/documents")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse updateDraftDocuments(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDtos.UpdateDraftDocumentsRequest req
    ) {
        return schemeApplicationService.updateDraftDocuments(id, req);
    }

    @PostMapping("/my/{id}/submit")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse submitDraft(@PathVariable Long id) {
        return schemeApplicationService.submitDraft(id);
    }

    @PostMapping("/my/{id}/cancel")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse cancel(
            @PathVariable Long id,
            @RequestBody(required = false) ApplicationDtos.CancelApplicationRequest req
    ) {
        return schemeApplicationService.cancelMyApplication(id, req);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<ApplicationDtos.ApplicationResponse> myApplications() {
        return schemeApplicationService.listMine();
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationDtos.ApplicationResponse myApplication(@PathVariable Long id) {
        return schemeApplicationService.getMine(id);
    }
}
