package com.saas.notification.entity;

/**
 * Enumeration of notification statuses.
 * 
 * Tracks the lifecycle of a notification from creation to delivery.
 */
public enum NotificationStatus {
    
    /**
     * Notification has been created but not yet processed.
     */
    PENDING("Pending", "Notification is queued for processing"),
    
    /**
     * Notification is currently being processed.
     */
    PROCESSING("Processing", "Notification is being processed"),
    
    /**
     * Notification has been successfully delivered.
     */
    DELIVERED("Delivered", "Notification has been delivered successfully"),
    
    /**
     * Notification has been read by the user.
     */
    READ("Read", "Notification has been read by the user"),
    
    /**
     * Notification delivery failed.
     */
    FAILED("Failed", "Notification delivery failed"),
    
    /**
     * Notification has been cancelled.
     */
    CANCELLED("Cancelled", "Notification was cancelled"),
    
    /**
     * Notification has expired.
     */
    EXPIRED("Expired", "Notification has expired");
    
    private final String displayName;
    private final String description;
    
    NotificationStatus(String displayName, String description) {
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
     * Check if this status represents a terminal state.
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == READ || this == FAILED || 
               this == CANCELLED || this == EXPIRED;
    }
    
    /**
     * Check if this status represents a failure state.
     */
    public boolean isFailure() {
        return this == FAILED || this == CANCELLED || this == EXPIRED;
    }
    
    /**
     * Check if this status represents a success state.
     */
    public boolean isSuccess() {
        return this == DELIVERED || this == READ;
    }
}
