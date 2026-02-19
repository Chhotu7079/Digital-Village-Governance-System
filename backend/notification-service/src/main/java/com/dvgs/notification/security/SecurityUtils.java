package com.dvgs.notification.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }
        return UUID.fromString(authentication.getName());
    }

    public static Set<String> getAuthorities(Authentication authentication) {
        if (authentication == null) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(Authentication authentication, String role) {
        return getAuthorities(authentication).contains(role.startsWith("ROLE_") ? role : "ROLE_" + role);
    }

    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }
}
