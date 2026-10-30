package com.libraryhub.util;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.libraryhub.service.LibraryUserDetails;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public static Optional<LibraryUserDetails> principal(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof LibraryUserDetails details ? Optional.of(details) : Optional.empty();
    }

    /** Where a signed-in person lands: the admin panel for admins, the member dashboard otherwise. */
    public static String homePathFor(Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return admin ? "/admin" : "/dashboard";
    }
}
