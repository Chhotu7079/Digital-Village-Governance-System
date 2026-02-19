package com.dvgs.notification.controller;

import com.dvgs.notification.domain.UserChannelPreference;
import com.dvgs.notification.dto.ChannelPreferenceRequestDto;
import com.dvgs.notification.security.SecurityUtils;
import com.dvgs.notification.service.ChannelPreferenceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class ChannelPreferenceController {

    private final ChannelPreferenceService preferenceService;

    public ChannelPreferenceController(ChannelPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserChannelPreference>> getPreferences(@PathVariable String userId,
                                                                      Authentication authentication) {
        enforcePreferenceAccess(userId, authentication);
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserChannelPreference>> updatePreferences(@PathVariable String userId,
                                                                         @Valid @RequestBody List<ChannelPreferenceRequestDto> requests,
                                                                         Authentication authentication) {
        enforcePreferenceAccess(userId, authentication);
        return ResponseEntity.ok(preferenceService.updatePreferences(userId, requests));
    }

    private void enforcePreferenceAccess(String userId, Authentication authentication) {
        UUID currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (!currentUserId.toString().equals(userId) && !SecurityUtils.hasAnyRole(authentication, "ADMIN", "OFFICIAL", "SUPER_ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Cannot access other user's preferences");
        }
    }
}
