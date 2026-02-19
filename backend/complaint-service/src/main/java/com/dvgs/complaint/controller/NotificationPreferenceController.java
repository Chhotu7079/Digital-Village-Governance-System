package com.dvgs.complaint.controller;

import com.dvgs.complaint.notification.NotificationPreference;
import com.dvgs.complaint.notification.NotificationPreferenceService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<NotificationPreference> get(Principal principal) {
        UUID citizenId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(preferenceService.getOrDefault(citizenId));
    }

    @PutMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<NotificationPreference> update(Principal principal,
                                                         @RequestBody NotificationPreference preference) {
        UUID citizenId = UUID.fromString(principal.getName());
        preference.setCitizenId(citizenId);
        return ResponseEntity.ok(preferenceService.save(preference));
    }
}
