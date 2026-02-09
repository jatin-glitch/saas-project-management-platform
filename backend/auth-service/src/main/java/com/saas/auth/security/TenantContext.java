package com.saas.auth.security;

/**
 * Tenant context holder for managing tenant information throughout the request lifecycle.
 * 
 * This class provides a thread-safe way to store and retrieve tenant information
 * during request processing. It uses ThreadLocal to ensure tenant isolation
 * across concurrent requests in a multi-tenant environment.
 * 
 * Key features:
 * - Thread-safe tenant storage using ThreadLocal
 * - Automatic cleanup to prevent memory leaks
 * - Integration with Spring Security context
 * - Support for tenant switching in administrative operations
 */
public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantIdentifier = new ThreadLocal<>();

    /**
     * Set the current tenant ID for the current thread.
     * 
     * @param tenantId the tenant ID to set
     */
    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Get the current tenant ID from the thread context.
     * 
     * @return the current tenant ID, or null if not set
     */
    public static Long getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Set the current tenant identifier (e.g., subdomain or custom domain).
     * 
     * @param tenantIdentifier the tenant identifier to set
     */
    public static void setCurrentTenantIdentifier(String tenantIdentifier) {
        currentTenantIdentifier.set(tenantIdentifier);
    }

    /**
     * Get the current tenant identifier from the thread context.
     * 
     * @return the current tenant identifier, or null if not set
     */
    public static String getCurrentTenantIdentifier() {
        return currentTenantIdentifier.get();
    }

    /**
     * Clear the current tenant context.
     * Should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        currentTenant.remove();
        currentTenantIdentifier.remove();
    }

    /**
     * Execute a task with a specific tenant context.
     * Automatically manages tenant context setup and cleanup.
     * 
     * @param tenantId the tenant ID to use for the execution
     * @param task the task to execute
     * @return the result of the task execution
     * @param <T> the return type of the task
     */
    public static <T> T executeWithTenant(Long tenantId, TenantTask<T> task) {
        Long previousTenant = getCurrentTenant();
        String previousIdentifier = getCurrentTenantIdentifier();
        
        try {
            setCurrentTenant(tenantId);
            return task.execute();
        } finally {
            // Restore previous context
            if (previousTenant != null) {
                setCurrentTenant(previousTenant);
            } else {
                currentTenant.remove();
            }
            
            if (previousIdentifier != null) {
                setCurrentTenantIdentifier(previousIdentifier);
            } else {
                currentTenantIdentifier.remove();
            }
        }
    }

    /**
     * Execute a task with a specific tenant context and identifier.
     * Automatically manages tenant context setup and cleanup.
     * 
     * @param tenantId the tenant ID to use for the execution
     * @param tenantIdentifier the tenant identifier to use
     * @param task the task to execute
     * @return the result of the task execution
     * @param <T> the return type of the task
     */
    public static <T> T executeWithTenant(Long tenantId, String tenantIdentifier, TenantTask<T> task) {
        Long previousTenant = getCurrentTenant();
        String previousIdentifier = getCurrentTenantIdentifier();
        
        try {
            setCurrentTenant(tenantId);
            setCurrentTenantIdentifier(tenantIdentifier);
            return task.execute();
        } finally {
            // Restore previous context
            if (previousTenant != null) {
                setCurrentTenant(previousTenant);
            } else {
                currentTenant.remove();
            }
            
            if (previousIdentifier != null) {
                setCurrentTenantIdentifier(previousIdentifier);
            } else {
                currentTenantIdentifier.remove();
            }
        }
    }

    /**
     * Functional interface for tasks executed within a tenant context.
     * 
     * @param <T> the return type of the task
     */
    @FunctionalInterface
    public interface TenantTask<T> {
        T execute();
    }

    /**
     * Check if a tenant context is currently set.
     * 
     * @return true if tenant context is set, false otherwise
     */
    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }

    /**
     * Get tenant information as a formatted string for logging.
     * 
     * @return formatted tenant information
     */
    public static String getTenantInfo() {
        Long tenantId = getCurrentTenant();
        String identifier = getCurrentTenantIdentifier();
        
        if (tenantId == null && identifier == null) {
            return "No tenant context";
        }
        
        StringBuilder sb = new StringBuilder();
        if (tenantId != null) {
            sb.append("tenantId=").append(tenantId);
        }
        if (identifier != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("identifier=").append(identifier);
        }
        
        return sb.toString();
    }
}
