package com.saas.common.enums;

import java.util.Set;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * User roles for the multi-tenant SaaS platform.
 * Provides role hierarchy and permission mapping logic.
 */
public enum Role {
    SUPER_ADMIN("Super Administrator", "System-wide administrator with all permissions"),
    TENANT_ADMIN("Tenant Administrator", "Tenant administrator with full tenant permissions"),
    MANAGER("Manager", "Project manager with team management permissions"),
    DEVELOPER("Developer", "Developer with task and issue management permissions"),
    VIEWER("Viewer", "Read-only access to projects and tasks");
    
    private final String displayName;
    private final String description;
    
    // Role hierarchy (higher index = higher privilege)
    private static final List<Role> HIERARCHY = Collections.unmodifiableList(
        Arrays.asList(VIEWER, DEVELOPER, MANAGER, TENANT_ADMIN, SUPER_ADMIN)
    );
    
    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this role has higher or equal privilege than another role.
     * 
     * @param otherRole the role to compare against
     * @return true if this role has higher or equal privilege
     */
    public boolean hasHigherOrEqualPrivilegeThan(Role otherRole) {
        if (otherRole == null) return true;
        return HIERARCHY.indexOf(this) >= HIERARCHY.indexOf(otherRole);
    }
    
    /**
     * Check if this role has higher privilege than another role.
     * 
     * @param otherRole the role to compare against
     * @return true if this role has higher privilege
     */
    public boolean hasHigherPrivilegeThan(Role otherRole) {
        if (otherRole == null) return true;
        return HIERARCHY.indexOf(this) > HIERARCHY.indexOf(otherRole);
    }
    
    /**
     * Check if this role can manage users with the specified role.
     * 
     * @param targetRole the role of the user to be managed
     * @return true if this role can manage the target role
     */
    public boolean canManageRole(Role targetRole) {
        if (targetRole == null) return false;
        return this.hasHigherPrivilegeThan(targetRole);
    }
    
    /**
     * Get all roles that this role can manage.
     * 
     * @return set of manageable roles
     */
    public Set<Role> getManageableRoles() {
        int currentIndex = HIERARCHY.indexOf(this);
        if (currentIndex <= 0) {
            return EnumSet.allOf(Role.class); // SUPER_ADMIN can manage all
        }
        return EnumSet.copyOf(HIERARCHY.subList(0, currentIndex));
    }
    
    /**
     * Check if a user with this role can perform the specified action.
     * 
     * @param permission the permission to check
     * @return true if the role grants the permission
     */
    public boolean hasPermission(Permission permission) {
        return Permission.hasPermission(this, permission);
    }
    
    /**
     * Get all permissions for this role.
     * 
     * @return set of permissions
     */
    public Set<Permission> getPermissions() {
        return Permission.getPermissionsForRole(this);
    }
    
    /**
     * Find role by display name.
     * 
     * @param displayName the display name to search for
     * @return the role or null if not found
     */
    public static Role fromDisplayName(String displayName) {
        for (Role role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * Get the highest role from a set of roles.
     * 
     * @param roles the set of roles
     * @return the highest role
     */
    public static Role getHighestRole(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        
        Role highest = null;
        for (Role role : roles) {
            if (highest == null || role.hasHigherPrivilegeThan(highest)) {
                highest = role;
            }
        }
        return highest;
    }
    
    /**
     * Check if a set of roles contains a role that can manage the target role.
     * 
     * @param userRoles the user's roles
     * @param targetRole the role to be managed
     * @return true if any role can manage the target role
     */
    public static boolean canManageRole(Set<Role> userRoles, Role targetRole) {
        if (userRoles == null || targetRole == null) {
            return false;
        }
        
        for (Role role : userRoles) {
            if (role.canManageRole(targetRole)) {
                return true;
            }
        }
        return false;
    }
}
