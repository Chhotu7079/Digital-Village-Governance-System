package com.dvgs.scheme.controller;

import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.SchemeDtos;
import com.dvgs.scheme.service.SchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schemes")
@RequiredArgsConstructor
public class SchemeController {

    private final SchemeService schemeService;

    /**
     * Admin: create scheme catalog item
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SchemeDtos.SchemeResponse create(@Valid @RequestBody SchemeDtos.CreateSchemeRequest req) {
        return schemeService.create(req);
    }

    /**
     * Admin: update scheme catalog item
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SchemeDtos.SchemeResponse update(@PathVariable Long id, @Valid @RequestBody SchemeDtos.UpdateSchemeRequest req) {
        return schemeService.update(id, req);
    }

    /**
     * Authenticated users: list schemes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public org.springframework.data.domain.Page<SchemeDtos.SchemeResponse> list(
            @RequestParam(required = false) SchemeStatus status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Only admins should be able to see archived schemes in catalog.
        if (includeArchived) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            if (!isAdmin) {
                includeArchived = false;
            }
        }

        return schemeService.search(status, department, q, includeArchived, page, size);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SchemeDtos.SchemeResponse archive(@PathVariable Long id) {
        return schemeService.archive(id);
    }

    @PostMapping("/{id}/unarchive")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SchemeDtos.SchemeResponse unarchive(@PathVariable Long id) {
        return schemeService.unarchive(id);
    }

    /**
     * Authenticated users: scheme details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public SchemeDtos.SchemeResponse getById(@PathVariable Long id) {
        return schemeService.getById(id);
    }
}
