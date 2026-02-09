package com.saas.notification.entity;

/**
 * Enumeration of notification priorities.
 * 
 * Determines the urgency and delivery order of notifications.
 */
public enum NotificationPriority {
    
    /**
     * Critical notifications that require immediate attention.
     * Examples: System outages, security alerts, critical failures.
     */
    CRITICAL("Critical", 1, "Immediate attention required"),
    
    /**
     * High priority notifications that should be delivered quickly.
     * Examples: Important deadlines, urgent tasks, major issues.
     */
    HIGH("High", 2, "High priority - deliver promptly"),
    
    /**
     * Medium priority notifications for regular business operations.
     * Examples: Task assignments, project updates, normal issues.
     */
    MEDIUM("Medium", 3, "Normal priority - standard delivery"),
    
    /**
     * Low priority notifications that can be delayed.
     * Examples: Informational updates, summary reports, non-urgent reminders.
     */
    LOW("Low", 4, "Low priority - can be delayed");
    
    private final String displayName;
    private final int level;
    private final String description;
    
    NotificationPriority(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this priority is higher than the given priority.
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level < other.level;
    }
    
    /**
     * Check if this priority is critical or high.
     */
    public boolean isUrgent() {
        return this == CRITICAL || this == HIGH;
    }
}
