package com.pawsclaws.petshop.security;

/**
 * Holds the tenants (petshop) id for the current request thread.
 *
 * <p>The value is populated by {@link JwtAuthenticationFilter} from the signed
 * JWT and cleared at the end of the request. Services read the tenant from
 * here instead of from request parameters, which makes cross-tenant access
 * impossible by construction.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_PETSHOP = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setPetshopId(Long petshopId) {
        CURRENT_PETSHOP.set(petshopId);
    }

    public static Long getPetshopId() {
        return CURRENT_PETSHOP.get();
    }

    public static void clear() {
        CURRENT_PETSHOP.remove();
    }
}
