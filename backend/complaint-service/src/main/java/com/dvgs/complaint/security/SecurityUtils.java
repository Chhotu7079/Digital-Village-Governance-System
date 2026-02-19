package com.dvgs.complaint.security;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
