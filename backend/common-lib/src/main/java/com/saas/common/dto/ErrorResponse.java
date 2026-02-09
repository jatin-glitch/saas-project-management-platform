package com.saas.common.dto;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Standard error response format for API exceptions.
 * Provides error code enumeration and internationalization support.
 */
public class ErrorResponse {
    
    // Error code enumeration
    public enum ErrorCode {
        // Authentication errors (1000-1999)
        INVALID_CREDENTIALS("AUTH_1001", "Invalid credentials provided"),
        TOKEN_EXPIRED("AUTH_1002", "Authentication token has expired"),
        TOKEN_INVALID("AUTH_1003", "Invalid authentication token"),
        ACCESS_DENIED("AUTH_1004", "Access denied"),
        ACCOUNT_LOCKED("AUTH_1005", "Account is locked"),
        ACCOUNT_DISABLED("AUTH_1006", "Account is disabled"),
        
        // Authorization errors (2000-2999)
        INSUFFICIENT_PERMISSIONS("AUTH_2001", "Insufficient permissions for this operation"),
        ROLE_REQUIRED("AUTH_2002", "Specific role required for this operation"),
        TENANT_ACCESS_DENIED("AUTH_2003", "Access denied for this tenant"),
        
        // Validation errors (3000-3999)
        VALIDATION_FAILED("VAL_3001", "Validation failed"),
        INVALID_INPUT("VAL_3002", "Invalid input provided"),
        MISSING_REQUIRED_FIELD("VAL_3003", "Required field is missing"),
        INVALID_FORMAT("VAL_3004", "Invalid format"),
        
        // Business logic errors (4000-4999)
        RESOURCE_NOT_FOUND("BIZ_4001", "Resource not found"),
        RESOURCE_ALREADY_EXISTS("BIZ_4002", "Resource already exists"),
        OPERATION_NOT_ALLOWED("BIZ_4003", "Operation not allowed"),
        BUSINESS_RULE_VIOLATION("BIZ_4004", "Business rule violation"),
        
        // System errors (5000-5999)
        INTERNAL_SERVER_ERROR("SYS_5001", "Internal server error"),
        DATABASE_ERROR("SYS_5002", "Database operation failed"),
        EXTERNAL_SERVICE_ERROR("SYS_5003", "External service error"),
        RATE_LIMIT_EXCEEDED("SYS_5004", "Rate limit exceeded"),
        SERVICE_UNAVAILABLE("SYS_5005", "Service temporarily unavailable");
        
        private final String code;
        private final String defaultMessage;
        
        ErrorCode(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String details;
    private String path;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(ErrorCode errorCode, String message) {
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }
    
    /**
     * Create error response with internationalization support.
     * 
     * @param errorCode the error code
     * @param locale the locale for message translation
     * @return error response with localized message
     */
    public static ErrorResponse of(ErrorCode errorCode, Locale locale) {
        String localizedMessage = getLocalizedMessage(errorCode, locale);
        return new ErrorResponse(errorCode, localizedMessage);
    }
    
    /**
     * Create error response with default locale.
     * 
     * @param errorCode the error code
     * @return error response with default message
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, Locale.getDefault());
    }
    
    /**
     * Create error response with custom message.
     * 
     * @param errorCode the error code
     * @param customMessage the custom message
     * @return error response with custom message
     */
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(errorCode, customMessage);
    }
    
    /**
     * Create error response with details.
     * 
     * @param errorCode the error code
     * @param details additional error details
     * @return error response with details
     */
    public static ErrorResponse withDetails(ErrorCode errorCode, String details) {
        ErrorResponse response = of(errorCode);
        response.setDetails(details);
        return response;
    }
    
    /**
     * Create error response with path information.
     * 
     * @param errorCode the error code
     * @param path the request path
     * @return error response with path
     */
    public static ErrorResponse withPath(ErrorCode errorCode, String path) {
        ErrorResponse response = of(errorCode);
        response.setPath(path);
        return response;
    }
    
    /**
     * Get localized message for error code.
     * 
     * @param errorCode the error code
     * @param locale the locale
     * @return localized message
     */
    private static String getLocalizedMessage(ErrorCode errorCode, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages.errors", locale);
            String key = "error." + errorCode.getCode();
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            // Fallback to default message if translation not found
            return errorCode.getDefaultMessage();
        }
    }

    // Getters and setters
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
