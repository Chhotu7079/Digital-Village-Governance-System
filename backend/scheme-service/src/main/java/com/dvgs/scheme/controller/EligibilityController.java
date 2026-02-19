package com.dvgs.scheme.controller;

import com.dvgs.scheme.dto.EligibilityDtos;
import com.dvgs.scheme.service.EligibilityService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    /**
     * Admin: define eligibility rules for a scheme.
     */
    @PutMapping("/schemes/{schemeId}/rule")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public EligibilityDtos.EligibilityRuleResponse upsertRule(
            @PathVariable Long schemeId,
            @Valid @RequestBody EligibilityDtos.UpsertEligibilityRuleRequest req
    ) {
        return eligibilityService.upsertRule(schemeId, req);
    }

    @GetMapping("/schemes/{schemeId}/rule")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public EligibilityDtos.EligibilityRuleResponse getRule(@PathVariable Long schemeId) {
        return eligibilityService.getRule(schemeId);
    }

    /**
     * Citizen: get list of schemes + eligibility decisions.
     * Forwards the incoming Bearer token to auth-service.
     */
    @GetMapping("/schemes/eligible")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<EligibilityDtos.EligibleSchemeResponse> eligibleSchemes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return eligibilityService.eligibleSchemes(authorization);
    }
}
