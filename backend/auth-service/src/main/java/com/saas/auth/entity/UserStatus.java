package com.saas.auth.entity;

/**
 * User status enumeration for authentication and authorization.
 * Determines whether a user can authenticate and access the system.
 */
public enum UserStatus {
    ACTIVE,                // User can authenticate and access the system
    INACTIVE,              // User account exists but cannot authenticate
    SUSPENDED,             // User temporarily suspended due to policy violations
    PENDING_VERIFICATION   // User registered but email not yet verified
}
