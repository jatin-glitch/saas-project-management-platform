package com.saas.notification.entity;

/**
 * Enumeration of notification types in the system.
 * 
 * Each type represents a different category of notifications that may have
 * different delivery rules, priorities, and display formats.
 */
public enum NotificationType {
    
    /**
     * Project-related notifications (created, updated, deleted, etc.)
     */
    PROJECT_CREATED("Project Created", "A new project has been created"),
    PROJECT_UPDATED("Project Updated", "Project details have been updated"),
    PROJECT_DELETED("Project Deleted", "A project has been deleted"),
    PROJECT_ARCHIVED("Project Archived", "A project has been archived"),
    
    /**
     * Task-related notifications
     */
    TASK_ASSIGNED("Task Assigned", "You have been assigned a new task"),
    TASK_UNASSIGNED("Task Unassigned", "A task has been unassigned from you"),
    TASK_STATUS_CHANGED("Task Status Changed", "Task status has been updated"),
    TASK_COMPLETED("Task Completed", "A task has been marked as completed"),
    TASK_OVERDUE("Task Overdue", "A task is now overdue"),
    TASK_DUE_SOON("Task Due Soon", "A task is due soon"),
    
    /**
     * Issue-related notifications
     */
    ISSUE_CREATED("Issue Created", "A new issue has been reported"),
    ISSUE_ASSIGNED("Issue Assigned", "You have been assigned an issue"),
    ISSUE_RESOLVED("Issue Resolved", "An issue has been resolved"),
    ISSUE_CLOSED("Issue Closed", "An issue has been closed"),
    
    /**
     * System notifications
     */
    SYSTEM_MAINTENANCE("System Maintenance", "Scheduled system maintenance"),
    SYSTEM_ANNOUNCEMENT("System Announcement", "Important system announcement"),
    SYSTEM_ERROR("System Error", "A system error has occurred"),
    
    /**
     * User account notifications
     */
    USER_WELCOME("Welcome", "Welcome to the platform"),
    USER_PROFILE_UPDATED("Profile Updated", "Your profile has been updated"),
    USER_PASSWORD_CHANGED("Password Changed", "Your password has been changed"),
    
    /**
     * Team notifications
     */
    TEAM_MEMBER_ADDED("Team Member Added", "A new member has joined the team"),
    TEAM_MEMBER_REMOVED("Team Member Removed", "A team member has been removed"),
    TEAM_PERMISSION_CHANGED("Team Permission Changed", "Team permissions have been updated");
    
    private final String displayName;
    private final String defaultDescription;
    
    NotificationType(String displayName, String defaultDescription) {
        this.displayName = displayName;
        this.defaultDescription = defaultDescription;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDefaultDescription() {
        return defaultDescription;
    }
}
