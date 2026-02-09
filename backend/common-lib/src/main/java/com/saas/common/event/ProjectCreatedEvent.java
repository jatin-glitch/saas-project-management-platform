package com.saas.common.event;

import java.util.UUID;

/**
 * Event fired when a new project is created.
 * 
 * This event contains all the essential information about the newly created project
 * that other services might need for:
 * - Sending welcome notifications
 * - Creating default project settings
 * - Updating analytics and reporting
 * - Triggering workflow automations
 */
public class ProjectCreatedEvent extends DomainEvent {
    
    private final UUID projectId;
    private final String projectName;
    private final String projectCode;
    private final String description;
    private final UUID creatorId;
    private final String creatorName;
    private final String creatorEmail;
    
    public ProjectCreatedEvent(Long tenantId, UUID userId, String userEmail, 
                               String ipAddress, String userAgent,
                               UUID projectId, String projectName, String projectCode, 
                               String description, UUID creatorId, String creatorName, String creatorEmail) {
        super(tenantId, userId, userEmail, ipAddress, userAgent);
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectCode = projectCode;
        this.description = description;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.creatorEmail = creatorEmail;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public String getProjectDescription() {
        return description;
    }
    
    public UUID getCreatorId() {
        return creatorId;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public String getCreatorEmail() {
        return creatorEmail;
    }
    
    @Override
    public String getRoutingKey() {
        return "project.created";
    }
    
    @Override
    public String getDescription() {
        return String.format("Project '%s' (%s) created by %s", projectName, projectCode, creatorName);
    }
}
