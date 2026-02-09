package com.saas.notification.service;

import com.saas.common.event.*;
import com.saas.notification.entity.NotificationPriority;
import com.saas.notification.entity.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service for consuming domain events and creating notifications.
 * 
 * This service listens to domain events from other services and creates
 * appropriate notifications. It demonstrates event-driven architecture
 * with proper decoupling between services.
 * 
 * Key features:
 * - Event-driven notification creation
 * - Multi-tenant event processing
 * - Idempotent event handling
 * - Error handling and logging
 * 
 * Event flow:
 * 1. Business service publishes domain event
 * 2. Event broker routes event to appropriate consumers
 * 3. This service receives event and creates notifications
 * 4. Notifications are processed and delivered via configured channels
 */
@Service
public class EventConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventConsumerService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Handle project created events.
     * Creates notifications for project managers and team members.
     */
    public void handleProjectCreated(@Payload ProjectCreatedEvent event) {
        try {
            logger.info("Processing PROJECT_CREATED event: {} for tenant: {}", 
                       event.getEventId(), event.getTenantId());
            
            // Create notification for the project creator
            String title = "Project Created Successfully";
            String message = String.format("Your project '%s' has been created successfully. Project code: %s", 
                                        event.getProjectName(), event.getProjectCode());
            
            notificationService.createNotification(
                event.getTenantId(),
                event.getCreatorId(),
                event.getCreatorEmail(),
                NotificationType.PROJECT_CREATED,
                title,
                message,
                "PROJECT_SERVICE",
                event.getEventId(),
                "PROJECT",
                event.getProjectId().toString(),
                "/projects/" + event.getProjectId(),
                "IN_APP,EMAIL",
                NotificationPriority.MEDIUM
            );
            
            // In a real implementation, you might also notify:
            // - Team members if they were added during creation
            // - Admin users for monitoring
            // - Other stakeholders based on project settings
            
            logger.info("Successfully processed PROJECT_CREATED event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing PROJECT_CREATED event: {} - {}", 
                        event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle task assigned events.
     * Creates notifications for the assigned user.
     */
    public void handleTaskAssigned(@Payload TaskAssignedEvent event) {
        try {
            logger.info("Processing TASK_ASSIGNED event: {} for tenant: {}", 
                       event.getEventId(), event.getTenantId());
            
            // Determine priority based on task priority
            NotificationPriority priority = determinePriorityFromTaskPriority(event.getPriority());
            
            String title = "New Task Assigned";
            String message = String.format("You have been assigned a new task: '%s' in project '%s'", 
                                        event.getTaskTitle(), event.getProjectName());
            
            notificationService.createNotification(
                event.getTenantId(),
                event.getAssignedToId(),
                event.getAssignedToEmail(),
                NotificationType.TASK_ASSIGNED,
                title,
                message,
                "TASK_SERVICE",
                event.getEventId(),
                "TASK",
                event.getTaskId().toString(),
                "/tasks/" + event.getTaskId(),
                "IN_APP,EMAIL,WEBSOCKET",
                priority
            );
            
            logger.info("Successfully processed TASK_ASSIGNED event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing TASK_ASSIGNED event: {} - {}", 
                        event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle task status changed events.
     * Creates notifications for relevant stakeholders.
     */
    public void handleTaskStatusChanged(@Payload TaskStatusChangedEvent event) {
        try {
            logger.info("Processing TASK_STATUS_CHANGED event: {} for tenant: {}", 
                       event.getEventId(), event.getTenantId());
            
            // Notify the assigned user if someone else changed the status
            if (!event.getChangedById().equals(event.getAssignedToId())) {
                String title = "Task Status Updated";
                String message = String.format("Your task '%s' status has been changed from %s to %s by %s", 
                                            event.getTaskTitle(), event.getOldStatus(), 
                                            event.getNewStatus(), event.getChangedByName());
                
                NotificationPriority priority = NotificationPriority.MEDIUM;
                if ("DONE".equals(event.getNewStatus())) {
                    title = "Task Completed";
                    priority = NotificationPriority.HIGH;
                }
                
                notificationService.createNotification(
                    event.getTenantId(),
                    event.getAssignedToId(),
                    event.getAssignedToEmail(),
                    NotificationType.TASK_STATUS_CHANGED,
                    title,
                    message,
                    "TASK_SERVICE",
                    event.getEventId(),
                    "TASK",
                    event.getTaskId().toString(),
                    "/tasks/" + event.getTaskId(),
                    "IN_APP,WEBSOCKET",
                    priority
                );
            }
            
            // In a real implementation, you might also notify:
            // - Project managers for critical status changes
            // - Team members for task completion
            // - Quality assurance team for tasks moved to testing
            
            logger.info("Successfully processed TASK_STATUS_CHANGED event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing TASK_STATUS_CHANGED event: {} - {}", 
                        event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle issue created events.
     * Creates notifications for development team and assignees.
     */
    public void handleIssueCreated(@Payload IssueCreatedEvent event) {
        try {
            logger.info("Processing ISSUE_CREATED event: {} for tenant: {}", 
                       event.getEventId(), event.getTenantId());
            
            // Determine priority based on issue severity
            NotificationPriority priority = determinePriorityFromSeverity(event.getSeverity());
            
            List<String> deliveryChannels = Arrays.asList("IN_APP", "EMAIL", "WEBSOCKET");
            if (priority.isUrgent()) {
                deliveryChannels = Arrays.asList("IN_APP", "EMAIL", "WEBSOCKET");
            }
            
            // Notify the assignee if different from reporter
            if (!event.getReporterId().equals(event.getAssigneeId())) {
                String title = "New Issue Assigned";
                String message = String.format("A new issue '%s' has been assigned to you. Severity: %s", 
                                            event.getIssueTitle(), event.getSeverity());
                
                notificationService.createNotification(
                    event.getTenantId(),
                    event.getAssigneeId(),
                    event.getAssigneeEmail(),
                    NotificationType.ISSUE_ASSIGNED,
                    title,
                    message,
                    "ISSUE_SERVICE",
                    event.getEventId(),
                    "ISSUE",
                    event.getIssueId().toString(),
                    "/issues/" + event.getIssueId(),
                    String.join(",", deliveryChannels),
                    priority
                );
            }
            
            // Create general issue notification for monitoring
            String title = "New Issue Created";
            String message = String.format("A new issue '%s' has been reported in project '%s'. Severity: %s", 
                                        event.getIssueTitle(), event.getProjectName(), event.getSeverity());
            
            notificationService.createNotification(
                event.getTenantId(),
                event.getReporterId(),
                event.getReporterEmail(),
                NotificationType.ISSUE_CREATED,
                title,
                message,
                "ISSUE_SERVICE",
                event.getEventId(),
                "ISSUE",
                event.getIssueId().toString(),
                "/issues/" + event.getIssueId(),
                "IN_APP",
                priority
            );
            
            logger.info("Successfully processed ISSUE_CREATED event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing ISSUE_CREATED event: {} - {}", 
                        event.getEventId(), e.getMessage(), e);
        }
    }
    
    /**
     * Determine notification priority from task priority.
     */
    private NotificationPriority determinePriorityFromTaskPriority(String taskPriority) {
        if (taskPriority == null) {
            return NotificationPriority.MEDIUM;
        }
        
        switch (taskPriority.toUpperCase()) {
            case "CRITICAL":
            case "HIGH":
                return NotificationPriority.HIGH;
            case "LOW":
                return NotificationPriority.LOW;
            default:
                return NotificationPriority.MEDIUM;
        }
    }
    
    /**
     * Determine notification priority from issue severity.
     */
    private NotificationPriority determinePriorityFromSeverity(String severity) {
        if (severity == null) {
            return NotificationPriority.MEDIUM;
        }
        
        switch (severity.toUpperCase()) {
            case "CRITICAL":
            case "BLOCKER":
                return NotificationPriority.CRITICAL;
            case "HIGH":
            case "MAJOR":
                return NotificationPriority.HIGH;
            case "LOW":
            case "MINOR":
                return NotificationPriority.LOW;
            default:
                return NotificationPriority.MEDIUM;
        }
    }
    
    // Functional Spring Cloud Stream consumers
    
    @Bean
    public Consumer<ProjectCreatedEvent> projectCreated() {
        return this::handleProjectCreated;
    }
    
    @Bean
    public Consumer<TaskAssignedEvent> taskAssigned() {
        return this::handleTaskAssigned;
    }
    
    @Bean
    public Consumer<TaskStatusChangedEvent> taskStatusChanged() {
        return this::handleTaskStatusChanged;
    }
    
    @Bean
    public Consumer<IssueCreatedEvent> issueCreated() {
        return this::handleIssueCreated;
    }
}
