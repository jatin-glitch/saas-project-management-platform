package com.saas.common.exception;

import com.saas.common.dto.ErrorResponse;
import com.saas.common.enums.Permission;
import com.saas.common.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Exception thrown when a user attempts to access resources without proper authorization.
 * Provides permission-based error details and audit logging.
 */
public class UnauthorizedException extends RuntimeException {
    
    private static final Logger logger = LoggerFactory.getLogger(UnauthorizedException.class);
    
    private String requiredPermission;
    private String resource;
    private String action;
    private String userId;
    private String tenantId;
    private LocalDateTime timestamp;
    private String clientIp;
    private String userAgent;
    private String requestId;
    
    public UnauthorizedException(String message) {
        super(message);
        this.timestamp = LocalDateTime.now();
        logSecurityEvent("GENERIC_UNAUTHORIZED", message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = LocalDateTime.now();
        logSecurityEvent("GENERIC_UNAUTHORIZED", message);
    }
    
    public UnauthorizedException(Permission requiredPermission, String resource, String action) {
        super(buildPermissionMessage(requiredPermission, resource, action));
        this.requiredPermission = requiredPermission.getCode();
        this.resource = resource;
        this.action = action;
        this.timestamp = LocalDateTime.now();
        logPermissionDenied(requiredPermission, resource, action);
    }
    
    public UnauthorizedException(Permission requiredPermission, String resource, String action, String userId, String tenantId) {
        this(requiredPermission, resource, action);
        this.userId = userId;
        this.tenantId = tenantId;
    }
    
    public UnauthorizedException(Permission requiredPermission, String resource, String action, 
                                 String userId, String tenantId, String clientIp, String userAgent) {
        this(requiredPermission, resource, action, userId, tenantId);
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }
    
    /**
     * Create unauthorized exception for role-based access denial.
     * 
     * @param requiredRole the required role
     * @param resource the resource being accessed
     * @param action the action being attempted
     * @return unauthorized exception
     */
    public static UnauthorizedException forRole(Role requiredRole, String resource, String action) {
        String message = String.format("Access denied: Role '%s' is required for %s on %s", 
            requiredRole.getDisplayName(), action, resource);
        UnauthorizedException exception = new UnauthorizedException(message);
        exception.setResource(resource);
        exception.setAction(action);
        exception.setRequiredPermission("ROLE_" + requiredRole.name());
        exception.logRoleAccessDenied(requiredRole, resource, action);
        return exception;
    }
    
    /**
     * Create unauthorized exception for permission-based access denial.
     * 
     * @param requiredPermission the required permission
     * @param resource the resource being accessed
     * @param action the action being attempted
     * @return unauthorized exception
     */
    public static UnauthorizedException forPermission(Permission requiredPermission, String resource, String action) {
        return new UnauthorizedException(requiredPermission, resource, action);
    }
    
    /**
     * Create unauthorized exception for tenant access denial.
     * 
     * @param tenantId the tenant ID
     * @param resource the resource being accessed
     * @return unauthorized exception
     */
    public static UnauthorizedException forTenant(String tenantId, String resource) {
        String message = String.format("Access denied: No access to tenant '%s' resource '%s'", tenantId, resource);
        UnauthorizedException exception = new UnauthorizedException(message);
        exception.setTenantId(tenantId);
        exception.setResource(resource);
        exception.setAction("TENANT_ACCESS");
        exception.setRequiredPermission("TENANT_ACCESS");
        exception.logTenantAccessDenied(tenantId, resource);
        return exception;
    }
    
    /**
     * Build permission-based error message.
     * 
     * @param permission the required permission
     * @param resource the resource
     * @param action the action
     * @return formatted error message
     */
    private static String buildPermissionMessage(Permission permission, String resource, String action) {
        return String.format("Access denied: Permission '%s' (%s) is required for %s on %s", 
            permission.getCode(), permission.getDescription(), action, resource);
    }
    
    /**
     * Log permission denied security event.
     * 
     * @param permission the denied permission
     * @param resource the resource
     * @param action the action
     */
    private void logPermissionDenied(Permission permission, String resource, String action) {
        if (logger.isWarnEnabled()) {
            logger.warn("SECURITY_AUDIT: Permission denied - Permission: {}, Resource: {}, Action: {}, User: {}, Tenant: {}, IP: {}, Time: {}", 
                permission.getCode(), resource, action, userId, tenantId, clientIp, timestamp);
        }
    }
    
    /**
     * Log role access denied security event.
     * 
     * @param requiredRole the required role
     * @param resource the resource
     * @param action the action
     */
    private void logRoleAccessDenied(Role requiredRole, String resource, String action) {
        if (logger.isWarnEnabled()) {
            logger.warn("SECURITY_AUDIT: Role access denied - Required Role: {}, Resource: {}, Action: {}, User: {}, Tenant: {}, IP: {}, Time: {}", 
                requiredRole.getDisplayName(), resource, action, userId, tenantId, clientIp, timestamp);
        }
    }
    
    /**
     * Log tenant access denied security event.
     * 
     * @param tenantId the tenant ID
     * @param resource the resource
     */
    private void logTenantAccessDenied(String tenantId, String resource) {
        if (logger.isWarnEnabled()) {
            logger.warn("SECURITY_AUDIT: Tenant access denied - Tenant: {}, Resource: {}, User: {}, IP: {}, Time: {}", 
                tenantId, resource, userId, clientIp, timestamp);
        }
    }
    
    /**
     * Log generic security event.
     * 
     * @param eventType the event type
     * @param message the event message
     */
    private void logSecurityEvent(String eventType, String message) {
        if (logger.isWarnEnabled()) {
            logger.warn("SECURITY_AUDIT: {} - Message: {}, User: {}, Tenant: {}, IP: {}, Time: {}", 
                eventType, message, userId, tenantId, clientIp, timestamp);
        }
    }
    
    /**
     * Convert to ErrorResponse.
     * 
     * @return error response representation
     */
    public ErrorResponse toErrorResponse() {
        ErrorResponse response = ErrorResponse.of(ErrorResponse.ErrorCode.ACCESS_DENIED);
        response.setDetails(buildAuditDetails());
        return response;
    }
    
    /**
     * Build audit details for logging.
     * 
     * @return audit details string
     */
    private String buildAuditDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Required Permission: ").append(requiredPermission != null ? "None" : requiredPermission);
        if (resource != null) {
            details.append(", Resource: ").append(resource);
        }
        if (action != null) {
            details.append(", Action: ").append(action);
        }
        if (userId != null) {
            details.append(", User: ").append(userId);
        }
        if (tenantId != null) {
            details.append(", Tenant: ").append(tenantId);
        }
        if (clientIp != null) {
            details.append(", IP: ").append(clientIp);
        }
        details.append(", Timestamp: ").append(timestamp);
        return details.toString();
    }
    
    // Getters and setters
    public String getRequiredPermission() {
        return requiredPermission;
    }
    
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
