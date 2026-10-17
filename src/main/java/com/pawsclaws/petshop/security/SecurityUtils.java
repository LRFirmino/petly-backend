package com.pawsclaws.petshop.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience accessors for the currently authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Não autenticado");
        }
        return principal;
    }

    /**
     * Tenant id of the current request. This is the ONLY source of truth for
     * scoping data access.
     */
    public static Long currentPetshopId() {
        return currentUser().getPetshopId();
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }
}
