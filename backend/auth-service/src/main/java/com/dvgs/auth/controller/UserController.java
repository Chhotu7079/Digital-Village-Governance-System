package com.dvgs.auth.controller;

import com.dvgs.auth.dto.EligibilityProfileDto;
import com.dvgs.auth.service.AuthService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me/eligibility-profile")
    public ResponseEntity<EligibilityProfileDto> eligibilityProfile(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(authService.getEligibilityProfile(userId));
    }
}
