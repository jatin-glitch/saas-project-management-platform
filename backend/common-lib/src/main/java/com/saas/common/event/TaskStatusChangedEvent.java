package com.saas.common.event;

import java.util.UUID;

/**
 * Event fired when a task's status is changed.
 * 
 * This event is important for:
 * - Progress tracking and analytics
 * - Workflow automation triggers
 * - Stakeholder notifications
 * - Sprint burndown calculations
 */
public class TaskStatusChangedEvent extends DomainEvent {
    
    private final UUID taskId;
    private final String taskTitle;
    private final String taskNumber;
    private final UUID projectId;
    private final String projectName;
    private final String oldStatus;
    private final String newStatus;
    private final UUID changedById;
    private final String changedByName;
    private final String changedByEmail;
    private final UUID assignedToId;
    private final String assignedToName;
    private final String assignedToEmail;
    
    public TaskStatusChangedEvent(Long tenantId, UUID userId, String userEmail, 
                                  String ipAddress, String userAgent,
                                  UUID taskId, String taskTitle, String taskNumber,
                                  UUID projectId, String projectName,
                                  String oldStatus, String newStatus,
                                  UUID changedById, String changedByName, String changedByEmail,
                                  UUID assignedToId, String assignedToName, String assignedToEmail) {
        super(tenantId, userId, userEmail, ipAddress, userAgent);
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskNumber = taskNumber;
        this.projectId = projectId;
        this.projectName = projectName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
        this.changedByName = changedByName;
        this.changedByEmail = changedByEmail;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignedToEmail = assignedToEmail;
    }
    
    public UUID getTaskId() {
        return taskId;
    }
    
    public String getTaskTitle() {
        return taskTitle;
    }
    
    public String getTaskNumber() {
        return taskNumber;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public String getOldStatus() {
        return oldStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public UUID getChangedById() {
        return changedById;
    }
    
    public String getChangedByName() {
        return changedByName;
    }
    
    public String getChangedByEmail() {
        return changedByEmail;
    }
    
    public UUID getAssignedToId() {
        return assignedToId;
    }
    
    public String getAssignedToName() {
        return assignedToName;
    }
    
    public String getAssignedToEmail() {
        return assignedToEmail;
    }
    
    @Override
    public String getRoutingKey() {
        return "task.status.changed";
    }
    
    @Override
    public String getDescription() {
        return String.format("Task '%s' (%s) status changed from %s to %s by %s", 
                           taskTitle, taskNumber, oldStatus, newStatus, changedByName);
    }
}
