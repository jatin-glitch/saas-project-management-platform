package com.saas.common.exception;

import com.saas.common.dto.ErrorResponse;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Base business exception for application-specific errors.
 * Provides error code enumeration and internationalization support.
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    private String details;
    private Object[] args;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(ErrorResponse.ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode.getCode();
    }
    
    public BusinessException(ErrorResponse.ErrorCode errorCode, String details) {
        this(errorCode);
        this.details = details;
    }
    
    public BusinessException(ErrorResponse.ErrorCode errorCode, Object[] args) {
        this(errorCode);
        this.args = args;
    }
    
    public BusinessException(ErrorResponse.ErrorCode errorCode, String details, Throwable cause) {
        this(errorCode, details);
        this.initCause(cause);
    }
    
    /**
     * Create business exception with localized message.
     * 
     * @param errorCode the error code
     * @param locale the locale for message translation
     * @return business exception with localized message
     */
    public static BusinessException of(ErrorResponse.ErrorCode errorCode, Locale locale) {
        String localizedMessage = getLocalizedMessage(errorCode, locale);
        BusinessException exception = new BusinessException(errorCode);
        exception.setLocalizedMessage(localizedMessage);
        return exception;
    }
    
    /**
     * Create business exception with default locale.
     * 
     * @param errorCode the error code
     * @return business exception with default message
     */
    public static BusinessException of(ErrorResponse.ErrorCode errorCode) {
        return of(errorCode, Locale.getDefault());
    }
    
    /**
     * Create business exception with arguments for message formatting.
     * 
     * @param errorCode the error code
     * @param args message arguments
     * @return business exception with formatted message
     */
    public static BusinessException of(ErrorResponse.ErrorCode errorCode, Object[] args) {
        BusinessException exception = new BusinessException(errorCode, args);
        String formattedMessage = formatMessage(errorCode, args, Locale.getDefault());
        exception.setLocalizedMessage(formattedMessage);
        return exception;
    }
    
    /**
     * Get localized message for error code.
     * 
     * @param errorCode the error code
     * @param locale the locale
     * @return localized message
     */
    private static String getLocalizedMessage(ErrorResponse.ErrorCode errorCode, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages.errors", locale);
            String key = "error." + errorCode.getCode();
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            // Fallback to default message if translation not found
            return errorCode.getDefaultMessage();
        }
    }
    
    /**
     * Format message with arguments.
     * 
     * @param errorCode the error code
     * @param args the message arguments
     * @param locale the locale
     * @return formatted message
     */
    private static String formatMessage(ErrorResponse.ErrorCode errorCode, Object[] args, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages.errors", locale);
            String key = "error." + errorCode.getCode();
            String pattern = bundle.getString(key);
            return java.text.MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            // Fallback to default message if translation not found
            String defaultMessage = errorCode.getDefaultMessage();
            return args != null && args.length > 0 ? 
                java.text.MessageFormat.format(defaultMessage, args) : defaultMessage;
        }
    }
    
    @Override
    public String getMessage() {
        if (args != null && super.getMessage() != null) {
            return formatMessage(ErrorResponse.ErrorCode.valueOf(errorCode), args, Locale.getDefault());
        }
        return super.getMessage();
    }
    
    /**
     * Override to set the localized message properly.
     */
    private void setLocalizedMessage(String message) {
        try {
            // Use reflection to access the protected message field in RuntimeException
            java.lang.reflect.Field messageField = RuntimeException.class.getDeclaredField("detailMessage");
            messageField.setAccessible(true);
            messageField.set(this, message);
        } catch (Exception e) {
            // Fallback: store in custom field if reflection fails
            this.details = message;
        }
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getDetails() {
        return details;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public void setArgs(Object[] args) {
        this.args = args;
    }
    
    /**
     * Convert to ErrorResponse.
     * 
     * @return error response representation
     */
    public ErrorResponse toErrorResponse() {
        if (errorCode != null) {
            ErrorResponse.ErrorCode code = ErrorResponse.ErrorCode.valueOf(errorCode);
            return ErrorResponse.of(code, getMessage());
        }
        return ErrorResponse.of(ErrorResponse.ErrorCode.INTERNAL_SERVER_ERROR, getMessage());
    }
}
