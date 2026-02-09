package com.saas.common.event;

import java.util.UUID;

/**
 * Event fired when a task is assigned to a user.
 * 
 * This event is crucial for:
 * - Sending assignment notifications
 * - Updating user workload metrics
 * - Triggering assignment workflows
 * - Maintaining audit trails
 */
public class TaskAssignedEvent extends DomainEvent {
    
    private final UUID taskId;
    private final String taskTitle;
    private final String taskNumber;
    private final UUID projectId;
    private final String projectName;
    private final UUID assignedToId;
    private final String assignedToName;
    private final String assignedToEmail;
    private final UUID assignedById;
    private final String assignedByName;
    private final String priority;
    
    public TaskAssignedEvent(Long tenantId, UUID userId, String userEmail, 
                             String ipAddress, String userAgent,
                             UUID taskId, String taskTitle, String taskNumber,
                             UUID projectId, String projectName,
                             UUID assignedToId, String assignedToName, String assignedToEmail,
                             UUID assignedById, String assignedByName, String priority) {
        super(tenantId, userId, userEmail, ipAddress, userAgent);
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskNumber = taskNumber;
        this.projectId = projectId;
        this.projectName = projectName;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignedToEmail = assignedToEmail;
        this.assignedById = assignedById;
        this.assignedByName = assignedByName;
        this.priority = priority;
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
    
    public UUID getAssignedToId() {
        return assignedToId;
    }
    
    public String getAssignedToName() {
        return assignedToName;
    }
    
    public String getAssignedToEmail() {
        return assignedToEmail;
    }
    
    public UUID getAssignedById() {
        return assignedById;
    }
    
    public String getAssignedByName() {
        return assignedByName;
    }
    
    public String getPriority() {
        return priority;
    }
    
    @Override
    public String getRoutingKey() {
        return "task.assigned";
    }
    
    @Override
    public String getDescription() {
        return String.format("Task '%s' (%s) assigned to %s by %s", 
                           taskTitle, taskNumber, assignedToName, assignedByName);
    }
}
