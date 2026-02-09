package com.saas.auth.entity;

/**
 * User role enumeration for role-based access control (RBAC).
 * Roles are hierarchical with increasing permissions.
 * Each role includes all permissions of lower roles.
 */
public enum UserRole {
    USER,           // Basic user access - can view assigned projects/tasks
    PROJECT_MANAGER, // Can manage projects within tenant - create/edit projects, manage team members
    ADMIN,          // Full tenant administration - manage all tenant resources, users, billing
    SUPER_ADMIN     // Cross-tenant administration - system-wide administration, manage multiple tenants
}
