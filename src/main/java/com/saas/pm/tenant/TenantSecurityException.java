package com.saas.pm.tenant;

/**
 * Exception thrown when tenant security violations are detected.
 * Used to prevent cross-tenant data access and unauthorized operations.
 * 
 * Why this exists:
 * - Provides specific error handling for tenant-related security issues
 * - Enables proper HTTP status codes for tenant violations
 * - Helps with debugging tenant isolation problems
 * - Supports audit logging for security incidents
 */
public class TenantSecurityException extends RuntimeException {
    
    public TenantSecurityException(String message) {
        super(message);
    }
    
    public TenantSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
