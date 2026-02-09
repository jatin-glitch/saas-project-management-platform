package com.saas.auth.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Example service demonstrating method-level security with role-based access control.
 * 
 * This service shows various ways to secure methods using Spring Security annotations:
 * - @PreAuthorize for complex expressions
 * - @Secured for simple role checks
 * - Custom security expressions
 * - Tenant-aware security checks
 */
@Service
public class AdminService {

    /**
     * Admin-only method using @PreAuthorize.
     * Only users with ADMIN role can access this method.
     * 
     * @return admin dashboard data
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Admin dashboard data");
        dashboard.put("userCount", 150);
        dashboard.put("activeProjects", 25);
        dashboard.put("revenue", 50000.0);
        
        return dashboard;
    }

    /**
     * Super admin-only method.
     * Only users with SUPER_ADMIN role can access this method.
     * 
     * @return system-wide statistics
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTenants", 50);
        stats.put("totalUsers", 5000);
        stats.put("systemUptime", "99.9%");
        stats.put("serverLoad", "45%");
        
        return stats;
    }

    /**
     * Method accessible by ADMIN or SUPER_ADMIN.
     * Uses OR condition in security expression.
     * 
     * @return tenant management data
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Map<String, Object> getTenantManagement() {
        Map<String, Object> management = new HashMap<>();
        management.put("activeTenants", 45);
        management.put("pendingApprovals", 3);
        management.put("suspendedTenants", 2);
        
        return management;
    }

    /**
     * Method using @Secured annotation.
     * Simple role-based security.
     * 
     * @return user management data
     */
    @Secured("ROLE_ADMIN")
    public Map<String, Object> getUserManagement() {
        Map<String, Object> userMgmt = new HashMap<>();
        userMgmt.put("totalUsers", 150);
        userMgmt.put("activeUsers", 142);
        userMgmt.put("newUsersThisMonth", 12);
        
        return userMgmt;
    }

    /**
     * Method with tenant-aware security.
     * Users can only access their own tenant's data.
     * 
     * @return tenant-specific data
     */
    @PreAuthorize("hasRole('ADMIN') and @tenantSecurityService.canAccessTenant(authentication, #tenantId)")
    public Map<String, Object> getTenantData(Long tenantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("tenantId", tenantId);
        data.put("tenantName", "Example Tenant");
        data.put("userCount", 25);
        data.put("projectCount", 8);
        
        return data;
    }

    /**
     * Method accessible by project managers and above.
     * Uses role hierarchy in security expression.
     * 
     * @return project management data
     */
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN', 'SUPER_ADMIN')")
    public Map<String, Object> getProjectManagement() {
        Map<String, Object> projects = new HashMap<>();
        projects.put("totalProjects", 15);
        projects.put("activeProjects", 12);
        projects.put("completedProjects", 3);
        
        return projects;
    }

    /**
     * Method that users can access their own data.
     * Uses authentication principal in security expression.
     * 
     * @param userId the user ID to access
     * @return user profile data
     */
    @PreAuthorize("#userId == authentication.principal.id")
    public Map<String, Object> getUserProfile(String userId) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", userId);
        profile.put("email", "user@example.com");
        profile.put("name", "John Doe");
        profile.put("role", "USER");
        
        return profile;
    }

    /**
     * Method with complex security logic.
     * Combines multiple conditions.
     * 
     * @return sensitive configuration data
     */
    @PreAuthorize("(hasRole('ADMIN') and @tenantSecurityService.isTenantActive(authentication.principal.tenantId)) " +
                   "or hasRole('SUPER_ADMIN')")
    public Map<String, Object> getSystemConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxUsersPerTenant", 100);
        config.put("defaultRole", "USER");
        config.put("sessionTimeout", 3600);
        config.put("passwordPolicy", "strong");
        
        return config;
    }

    
    /**
     * Method demonstrating custom security logic.
     * 
     * @return audit log data
     */
    @PreAuthorize("hasRole('ADMIN') and @tenantSecurityService.canViewAuditLogs(authentication.principal)")
    public Map<String, Object> getAuditLogs() {
        Map<String, Object> logs = new HashMap<>();
        logs.put("totalLogs", 1250);
        logs.put("logsToday", 45);
        logs.put("criticalEvents", 3);
        
        return logs;
    }
}
