package com.saas.auth.service;

import com.saas.auth.repository.UserRepository;
import com.saas.auth.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for tenant-aware security operations.
 * 
 * This service provides security checks that consider tenant boundaries
 * and ensures proper isolation between tenants in a multi-tenant SaaS application.
 * 
 * Features:
 * - Tenant access validation
 * - Tenant status checking
 * - Cross-tenant operation prevention
 * - Audit log access control
 */
@Service("tenantSecurityService")
public class TenantSecurityService {

    private final UserRepository userRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param userRepository the user repository
     */
    public TenantSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Check if the authenticated user can access the specified tenant.
     * 
     * @param authentication the Spring Security authentication object
     * @param tenantId the tenant ID to check access for
     * @return true if user can access the tenant, false otherwise
     */
    public boolean canAccessTenant(Authentication authentication, Long tenantId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // Super admins can access any tenant
        if (userPrincipal.isSuperAdmin()) {
            return true;
        }
        
        // Regular users can only access their own tenant
        return userPrincipal.getTenantId().equals(tenantId);
    }

    /**
     * Check if the tenant is active and operational.
     * 
     * @param tenantId the tenant ID to check
     * @return true if tenant is active, false otherwise
     */
    public boolean isTenantActive(Long tenantId) {
        // In a real implementation, you would check tenant status
        // For now, we'll assume all tenants are active
        return tenantId != null && tenantId > 0;
    }

    /**
     * Check if the user can view audit logs.
     * 
     * @param userPrincipal the authenticated user principal
     * @return true if user can view audit logs, false otherwise
     */
    public boolean canViewAuditLogs(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return false;
        }
        
        // Only admins and super admins can view audit logs
        return userPrincipal.isAdmin();
    }

    /**
     * Check if the user can manage users in the specified tenant.
     * 
     * @param authentication the Spring Security authentication object
     * @param tenantId the tenant ID
     * @return true if user can manage users in the tenant, false otherwise
     */
    public boolean canManageUsersInTenant(Authentication authentication, Long tenantId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // Super admins can manage users in any tenant
        if (userPrincipal.isSuperAdmin()) {
            return true;
        }
        
        // Regular admins can only manage users in their own tenant
        return userPrincipal.isAdmin() && userPrincipal.getTenantId().equals(tenantId);
    }

    /**
     * Check if the user can access financial data for the tenant.
     * 
     * @param authentication the Spring Security authentication object
     * @param tenantId the tenant ID
     * @return true if user can access financial data, false otherwise
     */
    public boolean canAccessFinancialData(Authentication authentication, Long tenantId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // Only admins and super admins can access financial data
        // and only for their own tenant (unless super admin)
        if (userPrincipal.isSuperAdmin()) {
            return true;
        }
        
        return userPrincipal.isAdmin() && userPrincipal.getTenantId().equals(tenantId);
    }

    /**
     * Get tenant statistics for the authenticated user.
     * 
     * @param authentication the Spring Security authentication object
     * @return tenant statistics
     */
    public Map<String, Object> getTenantStatistics(Authentication authentication) {
        Map<String, Object> stats = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            stats.put("error", "Not authenticated");
            return stats;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            stats.put("error", "Invalid principal");
            return stats;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        Long tenantId = userPrincipal.getTenantId();
        
        // Get tenant statistics
        long totalUsers = userRepository.countByTenantId(tenantId);
        long activeUsers = userRepository.countActiveUsersByTenant(tenantId);
        
        stats.put("tenantId", tenantId);
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        stats.put("userRole", userPrincipal.getRole().name());
        
        return stats;
    }

    /**
     * Check if the user can perform cross-tenant operations.
     * 
     * @param authentication the Spring Security authentication object
     * @return true if user can perform cross-tenant operations, false otherwise
     */
    public boolean canPerformCrossTenantOperations(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // Only super admins can perform cross-tenant operations
        return userPrincipal.isSuperAdmin();
    }

    /**
     * Validate tenant access for API operations.
     * 
     * @param authentication the Spring Security authentication object
     * @param requestedTenantId the tenant ID being requested
     * @return validation result with message
     */
    public Map<String, Object> validateTenantAccess(Authentication authentication, Long requestedTenantId) {
        Map<String, Object> result = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            result.put("valid", false);
            result.put("message", "Not authenticated");
            return result;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            result.put("valid", false);
            result.put("message", "Invalid authentication principal");
            return result;
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        if (!canAccessTenant(authentication, requestedTenantId)) {
            result.put("valid", false);
            result.put("message", "Access denied: Cannot access tenant " + requestedTenantId);
            return result;
        }
        
        if (!isTenantActive(requestedTenantId)) {
            result.put("valid", false);
            result.put("message", "Tenant is not active");
            return result;
        }
        
        result.put("valid", true);
        result.put("message", "Access granted");
        result.put("userTenantId", userPrincipal.getTenantId());
        result.put("requestedTenantId", requestedTenantId);
        
        return result;
    }
}
