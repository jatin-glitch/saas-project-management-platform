package com.saas.common.enums;

import java.util.Set;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * System permissions for the issue management platform.
 * Provides permission checking logic and role-permission mapping.
 */
public enum Permission {
    // Project permissions
    CREATE_PROJECT("project:create", "Create new projects"),
    UPDATE_PROJECT("project:update", "Update existing projects"),
    DELETE_PROJECT("project:delete", "Delete projects"),
    VIEW_PROJECT("project:view", "View project details"),
    
    // Task permissions
    CREATE_TASK("task:create", "Create new tasks"),
    ASSIGN_TASK("task:assign", "Assign tasks to users"),
    UPDATE_TASK("task:update", "Update existing tasks"),
    DELETE_TASK("task:delete", "Delete tasks"),
    VIEW_TASK("task:view", "View task details"),
    
    // Issue permissions
    CREATE_ISSUE("issue:create", "Create new issues"),
    UPDATE_ISSUE("issue:update", "Update existing issues"),
    CLOSE_ISSUE("issue:close", "Close issues"),
    DELETE_ISSUE("issue:delete", "Delete issues"),
    VIEW_ISSUE("issue:view", "View issue details"),
    
    // Analytics permissions
    VIEW_ANALYTICS("analytics:view", "View analytics and reports"),
    EXPORT_DATA("analytics:export", "Export data and reports"),
    
    // User management permissions
    MANAGE_USERS("user:manage", "Manage user accounts"),
    VIEW_USERS("user:view", "View user information"),
    
    // System permissions
    SYSTEM_ADMIN("system:admin", "System administration"),
    AUDIT_LOG("audit:view", "View audit logs");
    
    private final String code;
    private final String description;
    
    // Static mapping for role-permission assignments
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();
    
    static {
        // Initialize role-permission mappings
        ROLE_PERMISSIONS.put(Role.SUPER_ADMIN, EnumSet.allOf(Permission.class));
        
        ROLE_PERMISSIONS.put(Role.TENANT_ADMIN, EnumSet.of(
            CREATE_PROJECT, UPDATE_PROJECT, VIEW_PROJECT,
            CREATE_TASK, ASSIGN_TASK, UPDATE_TASK, DELETE_TASK, VIEW_TASK,
            CREATE_ISSUE, UPDATE_ISSUE, CLOSE_ISSUE, VIEW_ISSUE,
            VIEW_ANALYTICS, EXPORT_DATA,
            MANAGE_USERS, VIEW_USERS
        ));
        
        ROLE_PERMISSIONS.put(Role.MANAGER, EnumSet.of(
            CREATE_PROJECT, UPDATE_PROJECT, VIEW_PROJECT,
            CREATE_TASK, ASSIGN_TASK, UPDATE_TASK, VIEW_TASK,
            CREATE_ISSUE, UPDATE_ISSUE, CLOSE_ISSUE, VIEW_ISSUE,
            VIEW_ANALYTICS,
            VIEW_USERS
        ));
        
        ROLE_PERMISSIONS.put(Role.DEVELOPER, EnumSet.of(
            VIEW_PROJECT,
            CREATE_TASK, UPDATE_TASK, DELETE_TASK, VIEW_TASK,
            CREATE_ISSUE, UPDATE_ISSUE, CLOSE_ISSUE, VIEW_ISSUE
        ));
        
        ROLE_PERMISSIONS.put(Role.VIEWER, EnumSet.of(
            VIEW_PROJECT,
            VIEW_TASK,
            VIEW_ISSUE
        ));
    }
    
    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if a role has the specified permission.
     * 
     * @param role the role to check
     * @param permission the permission to check for
     * @return true if the role has the permission
     */
    public static boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Get all permissions for a given role.
     * 
     * @param role the role
     * @return set of permissions for the role
     */
    public static Set<Permission> getPermissionsForRole(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }
    
    /**
     * Check if a user has any of the specified permissions.
     * 
     * @param userRole the user's role
     * @param permissions the permissions to check
     * @return true if the user has any of the permissions
     */
    public static boolean hasAnyPermission(Role userRole, Permission... permissions) {
        Set<Permission> userPermissions = getPermissionsForRole(userRole);
        for (Permission permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a user has all of the specified permissions.
     * 
     * @param userRole the user's role
     * @param permissions the permissions to check
     * @return true if the user has all permissions
     */
    public static boolean hasAllPermissions(Role userRole, Permission... permissions) {
        Set<Permission> userPermissions = getPermissionsForRole(userRole);
        for (Permission permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Find permission by code.
     * 
     * @param code the permission code
     * @return the permission or null if not found
     */
    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.code.equals(code)) {
                return permission;
            }
        }
        return null;
    }
}
