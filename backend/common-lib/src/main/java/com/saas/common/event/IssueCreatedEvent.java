package com.saas.common.event;

import java.util.UUID;

/**
 * Event fired when a new issue is created.
 * 
 * This event is used for:
 * - Alerting development teams
 * - Creating support tickets
 * - Triggering issue triage workflows
 * - Maintaining quality metrics
 */
public class IssueCreatedEvent extends DomainEvent {
    
    private final UUID issueId;
    private final String issueTitle;
    private final String issueNumber;
    private final String description;
    private final String severity;
    private final String type;
    private final UUID projectId;
    private final String projectName;
    private final UUID reporterId;
    private final String reporterName;
    private final String reporterEmail;
    private final UUID assigneeId;
    private final String assigneeName;
    private final String assigneeEmail;
    
    public IssueCreatedEvent(Long tenantId, UUID userId, String userEmail, 
                             String ipAddress, String userAgent,
                             UUID issueId, String issueTitle, String issueNumber,
                             String description, String severity, String type,
                             UUID projectId, String projectName,
                             UUID reporterId, String reporterName, String reporterEmail,
                             UUID assigneeId, String assigneeName, String assigneeEmail) {
        super(tenantId, userId, userEmail, ipAddress, userAgent);
        this.issueId = issueId;
        this.issueTitle = issueTitle;
        this.issueNumber = issueNumber;
        this.description = description;
        this.severity = severity;
        this.type = type;
        this.projectId = projectId;
        this.projectName = projectName;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.assigneeEmail = assigneeEmail;
    }
    
    public UUID getIssueId() {
        return issueId;
    }
    
    public String getIssueTitle() {
        return issueTitle;
    }
    
    public String getIssueNumber() {
        return issueNumber;
    }
    
    public String getIssueDescription() {
        return description;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public String getType() {
        return type;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public UUID getReporterId() {
        return reporterId;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public String getReporterEmail() {
        return reporterEmail;
    }
    
    public UUID getAssigneeId() {
        return assigneeId;
    }
    
    public String getAssigneeName() {
        return assigneeName;
    }
    
    public String getAssigneeEmail() {
        return assigneeEmail;
    }
    
    @Override
    public String getRoutingKey() {
        return "issue.created";
    }
    
    @Override
    public String getDescription() {
        return String.format("Issue '%s' (%s) created by %s with severity %s", 
                           issueTitle, issueNumber, reporterName, severity);
    }
}
