package com.saas.common.tenant;

import com.saas.common.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * ThreadLocal-based tenant context for multi-tenancy support.
 * Provides tenant identification from JWT tokens and request headers with validation.
 */
public class TenantContext {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();
    
    // Default tenant for fallback
    private static final Long DEFAULT_TENANT_ID = 1L;
    
    // Tenant ID validation pattern
    private static final Pattern TENANT_ID_PATTERN = Pattern.compile("^[1-9]\\d*$");
    private static final Pattern TENANT_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");
    
    /**
     * Set the current tenant for the calling thread with validation.
     * 
     * @param tenantId the tenant ID to set
     * @throws IllegalArgumentException if tenant ID is invalid
     */
    public static void setCurrentTenant(Long tenantId) {
        validateTenantId(tenantId);
        CURRENT_TENANT.set(tenantId);
        CURRENT_TENANT_ID.set(tenantId != null ? tenantId.toString() : null);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Set current tenant to: {} for thread: {}", tenantId, Thread.currentThread().getName());
        }
    }
    
    /**
     * Set the current tenant using string identifier.
     * 
     * @param tenantIdentifier the tenant identifier
     * @throws IllegalArgumentException if tenant identifier is invalid
     */
    public static void setCurrentTenant(String tenantIdentifier) {
        validateTenantIdentifier(tenantIdentifier);
        
        try {
            Long tenantId = Long.parseLong(tenantIdentifier);
            setCurrentTenant(tenantId);
        } catch (NumberFormatException e) {
            // Handle non-numeric tenant identifiers
            CURRENT_TENANT.set(null);
            CURRENT_TENANT_ID.set(tenantIdentifier);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Set current tenant identifier to: {} for thread: {}", tenantIdentifier, Thread.currentThread().getName());
            }
        }
    }
    
    /**
     * Extract and set tenant from HTTP request headers.
     * 
     * @param request the HTTP request
     */
    public static void setTenantFromRequest(HttpServletRequest request) {
        if (request == null) {
            logger.warn("Request is null, cannot extract tenant");
            return;
        }
        
        // Try X-Tenant-Id header first
        String tenantHeader = request.getHeader(SecurityConstants.TENANT_ID_HEADER);
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            setCurrentTenant(tenantHeader);
            return;
        }
        
        // Try JWT token extraction (simplified)
        String authHeader = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());
            setTenantFromToken(token);
            return;
        }
        
        // Fallback to default tenant
        logger.warn("No tenant information found in request, using default tenant");
        setCurrentTenant(DEFAULT_TENANT_ID);
    }
    
    /**
     * Extract tenant from JWT token (simplified implementation).
     * In a real implementation, this would parse the JWT token properly.
     * 
     * @param token the JWT token
     */
    private static void setTenantFromToken(String token) {
        try {
            // This is a simplified implementation
            // In production, use a proper JWT library to extract claims
            if (token.length() > 10) { // Basic validation
                // For demo purposes, assume tenant ID is encoded in token
                // Real implementation would parse JWT claims
                setCurrentTenant(DEFAULT_TENANT_ID);
            }
        } catch (Exception e) {
            logger.warn("Failed to extract tenant from token: {}", e.getMessage());
            setCurrentTenant(DEFAULT_TENANT_ID);
        }
    }
    
    /**
     * Get the current tenant ID for the calling thread.
     * 
     * @return the current tenant ID, or default if not set
     */
    public static Long getCurrentTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            logger.debug("No tenant ID set, returning default tenant: {}", DEFAULT_TENANT_ID);
            return DEFAULT_TENANT_ID;
        }
        return tenantId;
    }
    
    /**
     * Get the current tenant identifier for the calling thread.
     * 
     * @return the current tenant identifier
     */
    public static String getCurrentTenant() {
        String tenantId = CURRENT_TENANT_ID.get();
        if (tenantId == null) {
            Long defaultTenant = getCurrentTenantId();
            return defaultTenant.toString();
        }
        return tenantId;
    }
    
    /**
     * Get the current tenant with fallback logic.
     * 
     * @return the current tenant (never null)
     */
    public static Long getCurrentTenantWithFallback() {
        return getCurrentTenantId();
    }
    
    /**
     * Check if a tenant is valid for the current context.
     * 
     * @param tenantId the tenant ID to check
     * @return true if valid
     */
    public static boolean isValidTenant(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        return tenantId > 0 && TENANT_ID_PATTERN.matcher(tenantId.toString()).matches();
    }
    
    /**
     * Clear the current tenant from the calling thread.
     * This should be called in request cleanup filters.
     */
    public static void clear() {
        Long tenantId = CURRENT_TENANT.get();
        CURRENT_TENANT.remove();
        CURRENT_TENANT_ID.remove();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Cleared tenant context: {} for thread: {}", tenantId, Thread.currentThread().getName());
        }
    }
    
    /**
     * Validate tenant ID before setting.
     * 
     * @param tenantId the tenant ID to validate
     * @throws IllegalArgumentException if invalid
     */
    private static void validateTenantId(Long tenantId) {
        if (tenantId != null && !isValidTenant(tenantId)) {
            throw new IllegalArgumentException("Invalid tenant ID: " + tenantId + ". Tenant ID must be a positive number.");
        }
    }
    
    /**
     * Validate tenant identifier before setting.
     * 
     * @param tenantIdentifier the tenant identifier to validate
     * @throws IllegalArgumentException if invalid
     */
    private static void validateTenantIdentifier(String tenantIdentifier) {
        if (tenantIdentifier != null && !tenantIdentifier.trim().isEmpty()) {
            if (!TENANT_IDENTIFIER_PATTERN.matcher(tenantIdentifier).matches()) {
                throw new IllegalArgumentException("Invalid tenant identifier: " + tenantIdentifier + ". Must be 3-50 characters of letters, numbers, hyphens, and underscores.");
            }
        }
    }
    
    /**
     * Get tenant information for logging/monitoring.
     * 
     * @return tenant info string
     */
    public static String getTenantInfo() {
        Long tenantId = getCurrentTenantId();
        String tenantIdentifier = getCurrentTenant();
        return String.format("Tenant[id=%s, identifier=%s]", tenantId, tenantIdentifier);
    }
}
