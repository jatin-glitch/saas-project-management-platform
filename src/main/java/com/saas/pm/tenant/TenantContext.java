package com.saas.pm.tenant;

/**
 * TenantContext provides thread-safe storage for the current tenant ID.
 * Uses ThreadLocal to ensure tenant isolation across concurrent requests.
 * This is the central point for tenant resolution throughout the application.
 * 
 * Why this exists:
 * - Provides request-scoped tenant identification
 * - Ensures thread safety in multi-threaded environments
 * - Enables automatic tenant ID injection into database operations
 * - Supports clean tenant context management
 * 
 * Scalability benefits:
 * - ThreadLocal ensures no cross-tenant data contamination
 * - Minimal memory overhead per request
 * - Fast access without database lookups
 * - Works seamlessly with connection pooling
 */
public class TenantContext {
    
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    
    /**
     * Sets the current tenant ID for this thread.
     * Called by TenantFilter at the beginning of each request.
     * 
     * @param tenantId The tenant identifier from request headers
     */
    public static void setCurrentTenant(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    /**
     * Gets the current tenant ID for this thread.
     * Used by Hibernate interceptor and business logic.
     * 
     * @return Current tenant ID or null if not set
     */
    public static Long getCurrentTenant() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * Clears the current tenant ID for this thread.
     * Called by TenantFilter at the end of each request to prevent memory leaks.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
    
    /**
     * Checks if a tenant context is available.
     * Useful for validation and error handling.
     * 
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
