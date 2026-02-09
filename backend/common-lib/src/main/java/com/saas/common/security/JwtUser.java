package com.saas.common.security;

import com.saas.common.enums.Role;
import com.saas.common.enums.Permission;

import java.util.Set;
import java.util.UUID;
import java.util.Date;
import java.util.Collections;

/**
 * JWT user representation for authentication and authorization.
 * Provides user details extraction from JWT tokens and role/permission mapping.
 */
public class JwtUser {
    
    private UUID userId;
    private String username;
    private String email;
    private Long tenantId;
    private Role role;
    private Set<Permission> permissions;
    private Date issuedAt;
    private Date expiresAt;
    
    public JwtUser() {}
    
    /**
     * Constructor for JWT token parsing (simplified implementation).
     * In a real implementation, this would use a JWT library like jjwt or nimbus-jose.
     * 
     * @param userId the user ID
     * @param username the username
     * @param email the email
     * @param tenantId the tenant ID
     * @param role the user role
     */
    public JwtUser(UUID userId, String username, String email, Long tenantId, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.tenantId = tenantId;
        this.role = role;
        this.permissions = role != null ? role.getPermissions() : Collections.emptySet();
        this.issuedAt = new Date();
        this.expiresAt = new Date(System.currentTimeMillis() + 86400000); // 24 hours
    }
    
    /**
     * Check if user has the specified permission.
     * 
     * @param permission the permission to check
     * @return true if user has the permission
     */
    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Check if user has any of the specified permissions.
     * 
     * @param permissions the permissions to check
     * @return true if user has any of the permissions
     */
    public boolean hasAnyPermission(Permission... permissions) {
        if (this.permissions == null || permissions == null) {
            return false;
        }
        for (Permission permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user has all of the specified permissions.
     * 
     * @param permissions the permissions to check
     * @return true if user has all permissions
     */
    public boolean hasAllPermissions(Permission... permissions) {
        if (this.permissions == null || permissions == null) {
            return false;
        }
        for (Permission permission : permissions) {
            if (!this.permissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if user has the specified role.
     * 
     * @param role the role to check
     * @return true if user has the role
     */
    public boolean hasRole(Role role) {
        return this.role != null && this.role.equals(role);
    }
    
    /**
     * Check if user has a role with higher or equal privilege.
     * 
     * @param requiredRole the required role
     * @return true if user's role has higher or equal privilege
     */
    public boolean hasRoleOrHigher(Role requiredRole) {
        return this.role != null && this.role.hasHigherOrEqualPrivilegeThan(requiredRole);
    }
    
    /**
     * Check if the token is expired.
     * 
     * @return true if token is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Date());
    }
    
    /**
     * Check if the token will expire within the specified minutes.
     * 
     * @param minutes the minutes to check
     * @return true if token expires within specified minutes
     */
    public boolean expiresWithin(int minutes) {
        if (expiresAt == null) return true;
        long expiryTime = expiresAt.getTime();
        long currentTime = System.currentTimeMillis();
        long thresholdTime = currentTime + (minutes * 60 * 1000L);
        return expiryTime <= thresholdTime;
    }
    
    // Getters and setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Set<Permission> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public Date getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public Date getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
