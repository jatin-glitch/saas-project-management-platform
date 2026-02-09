package com.saas.common.security;

/**
 * Security-related constants for the SaaS platform.
 * Contains JWT token settings, security headers, and role/permission mappings.
 */
public class SecurityConstants {
    
    // JWT Token Constants
    // Configured token expiration times for different environments
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION_MS = 604800000; // 7 days
    public static final long JWT_PASSWORD_RESET_EXPIRATION_MS = 3600000; // 1 hour
    public static final long JWT_EMAIL_VERIFICATION_EXPIRATION_MS = 86400000; // 24 hours
    
    // JWT Token Headers
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";
    
    // Security Headers for CORS and CSRF protection
    public static final String CORS_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String CORS_METHODS_HEADER = "Access-Control-Allow-Methods";
    public static final String CORS_HEADERS_HEADER = "Access-Control-Allow-Headers";
    public static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";
    public static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    
    // Role hierarchy constants
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_USER = "USER";
    
    // Permission mapping constants
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String PERMISSION_ADMIN = "ADMIN";
    
    // Security patterns
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOCK_TIME_DURATION_MS = 900000; // 15 minutes
    
    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
