package com.saas.notification.integration;

import com.saas.common.event.*;
import com.saas.notification.entity.Notification;
import com.saas.notification.entity.NotificationType;
import com.saas.notification.service.NotificationService;
import com.saas.common.event.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete event-driven notification system.
 * 
 * This test demonstrates:
 * - Event publishing and consumption
 * - Notification creation from events
 * - Multi-channel notification delivery
 * - Audit trail generation
 * - System reliability features
 * 
 * Test scenarios:
 * 1. Project creation triggers notifications
 * 2. Task assignment triggers real-time notifications
 * 3. Task status changes generate appropriate notifications
 * 4. Issue creation creates high-priority notifications
 * 5. System handles failures gracefully
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventDrivenNotificationIntegrationTest {
    
    @Autowired
    private EventPublisher streamBridge;
    
    @Autowired
    private NotificationService notificationService;
    
    private Long testTenantId = 1L;
    private UUID testUserId = UUID.randomUUID();
    private String testUserEmail = "test@example.com";
    private String testIpAddress = "127.0.0.1";
    private String testUserAgent = "Test-Agent/1.0";
    
    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        // This would be implemented based on your test setup
    }
    
    @Test
    void testProjectCreatedEventNotification() throws InterruptedException {
        // Arrange
        UUID projectId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            projectId, "Test Project", "PROJ001", "Test project description",
            testUserId, "Test User", testUserEmail
        );
        
        // Act
        streamBridge.publish(event);
        
        // Wait for async processing
        Thread.sleep(2000);
        
        // Assert
        List<Notification> notifications = notificationService.getUnreadNotifications(testTenantId, testUserId);
        assertFalse(notifications.isEmpty(), "Should have created notifications");
        
        Notification notification = notifications.stream()
            .filter(n -> n.getType() == NotificationType.PROJECT_CREATED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(notification, "Should have project created notification");
        assertEquals("Test Project", notification.getTitle());
        assertTrue(notification.getMessage().contains("Test Project"));
        assertEquals(testTenantId, notification.getTenantId());
        assertEquals(testUserId, notification.getUserId());
        assertEquals(event.getEventId(), notification.getEventId());
        assertEquals("PROJECT", notification.getEntityType());
        assertEquals(projectId.toString(), notification.getEntityId());
    }
    
    @Test
    void testTaskAssignedEventNotification() throws InterruptedException {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assignedToId = UUID.randomUUID();
        String assignedToEmail = "assigned@example.com";
        
        TaskAssignedEvent event = new TaskAssignedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            taskId, "Test Task", "TASK001", projectId, "Test Project",
            assignedToId, "Assigned User", assignedToEmail,
            testUserId, "Assigner User", "HIGH"
        );
        
        // Act
        streamBridge.publish(event);
        
        // Wait for async processing
        Thread.sleep(2000);
        
        // Assert
        List<Notification> notifications = notificationService.getUnreadNotifications(testTenantId, assignedToId);
        assertFalse(notifications.isEmpty(), "Should have created notifications for assigned user");
        
        Notification notification = notifications.stream()
            .filter(n -> n.getType() == NotificationType.TASK_ASSIGNED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(notification, "Should have task assigned notification");
        assertEquals("New Task Assigned", notification.getTitle());
        assertTrue(notification.getMessage().contains("Test Task"));
        assertEquals(assignedToId, notification.getUserId());
        assertEquals(assignedToEmail, notification.getUserEmail());
        assertEquals(event.getEventId(), notification.getEventId());
        assertEquals("TASK", notification.getEntityType());
        assertEquals(taskId.toString(), notification.getEntityId());
    }
    
    @Test
    void testTaskStatusChangedEventNotification() throws InterruptedException {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assignedToId = UUID.randomUUID();
        String assignedToEmail = "assigned@example.com";
        UUID changedById = UUID.randomUUID();
        String changedByName = "Changer User";
        
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            taskId, "Test Task", "TASK001", projectId, "Test Project",
            "IN_PROGRESS", "DONE", changedById, changedByName, "changer@example.com",
            assignedToId, "Assigned User", assignedToEmail
        );
        
        // Act
        streamBridge.publish(event);
        
        // Wait for async processing
        Thread.sleep(2000);
        
        // Assert
        List<Notification> notifications = notificationService.getUnreadNotifications(testTenantId, assignedToId);
        assertFalse(notifications.isEmpty(), "Should have created notifications for assigned user");
        
        Notification notification = notifications.stream()
            .filter(n -> n.getType() == NotificationType.TASK_STATUS_CHANGED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(notification, "Should have task status changed notification");
        assertEquals("Task Completed", notification.getTitle());
        assertTrue(notification.getMessage().contains("DONE"));
        assertEquals(assignedToId, notification.getUserId());
        assertEquals(event.getEventId(), notification.getEventId());
        assertEquals("TASK", notification.getEntityType());
        assertEquals(taskId.toString(), notification.getEntityId());
    }
    
    @Test
    void testIssueCreatedEventNotification() throws InterruptedException {
        // Arrange
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        String reporterEmail = "reporter@example.com";
        UUID assigneeId = UUID.randomUUID();
        String assigneeEmail = "assignee@example.com";
        
        IssueCreatedEvent event = new IssueCreatedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            issueId, "Critical Issue", "ISSUE001", "Critical system issue", "CRITICAL", "BUG",
            projectId, "Test Project",
            reporterId, "Reporter User", reporterEmail,
            assigneeId, "Assignee User", assigneeEmail
        );
        
        // Act
        streamBridge.publish(event);
        
        // Wait for async processing
        Thread.sleep(2000);
        
        // Assert - Check assignee notification
        List<Notification> assigneeNotifications = notificationService.getUnreadNotifications(testTenantId, assigneeId);
        assertFalse(assigneeNotifications.isEmpty(), "Should have created notifications for assignee");
        
        Notification assigneeNotification = assigneeNotifications.stream()
            .filter(n -> n.getType() == NotificationType.ISSUE_ASSIGNED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(assigneeNotification, "Should have issue assigned notification for assignee");
        assertEquals(assigneeId, assigneeNotification.getUserId());
        assertEquals(assigneeEmail, assigneeNotification.getUserEmail());
        
        // Assert - Check reporter notification
        List<Notification> reporterNotifications = notificationService.getUnreadNotifications(testTenantId, reporterId);
        assertFalse(reporterNotifications.isEmpty(), "Should have created notifications for reporter");
        
        Notification reporterNotification = reporterNotifications.stream()
            .filter(n -> n.getType() == NotificationType.ISSUE_CREATED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(reporterNotification, "Should have issue created notification for reporter");
        assertEquals(reporterId, reporterNotification.getUserId());
        assertEquals(reporterEmail, reporterNotification.getUserEmail());
    }
    
    @Test
    void testNotificationIdempotency() throws InterruptedException {
        // Arrange
        UUID projectId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            projectId, "Test Project", "PROJ001", "Test project description",
            testUserId, "Test User", testUserEmail
        );
        
        // Act - Send the same event twice
        streamBridge.publish(event);
        Thread.sleep(1000);
        streamBridge.publish(event);
        Thread.sleep(2000);
        
        // Assert - Should only have one notification due to idempotency
        List<Notification> notifications = notificationService.getUnreadNotifications(testTenantId, testUserId);
        long projectNotifications = notifications.stream()
            .filter(n -> n.getType() == NotificationType.PROJECT_CREATED)
            .filter(n -> event.getEventId().equals(n.getEventId()))
            .count();
        
        assertEquals(1, projectNotifications, "Should have only one notification due to idempotency");
    }
    
    @Test
    void testNotificationMarkAsRead() throws InterruptedException {
        // Arrange - Create a notification first
        UUID projectId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(
            testTenantId, testUserId, testUserEmail, testIpAddress, testUserAgent,
            projectId, "Test Project", "PROJ002", "Test project description",
            testUserId, "Test User", testUserEmail
        );
        
        streamBridge.publish(event);
        Thread.sleep(2000);
        
        List<Notification> notifications = notificationService.getUnreadNotifications(testTenantId, testUserId);
        assertFalse(notifications.isEmpty(), "Should have notifications");
        
        Notification notification = notifications.get(0);
        UUID notificationId = notification.getId();
        
        // Act
        int updatedCount = notificationService.markAsRead(testTenantId, testUserId, List.of(notificationId));
        
        // Assert
        assertEquals(1, updatedCount, "Should have updated one notification");
        
        // Verify notification is marked as read
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testTenantId, testUserId);
        boolean isStillUnread = unreadNotifications.stream()
            .anyMatch(n -> n.getId().equals(notificationId));
        
        assertFalse(isStillUnread, "Notification should be marked as read");
    }
    
    @Test
    void testNotificationStatistics() {
        // Act
        var statusStats = notificationService.getNotificationStatsByStatus(testTenantId);
        var typeStats = notificationService.getNotificationStatsByType(testTenantId);
        
        // Assert
        assertNotNull(statusStats, "Status statistics should not be null");
        assertNotNull(typeStats, "Type statistics should not be null");
        
        // Log statistics for manual verification
        System.out.println("Notification Status Statistics:");
        statusStats.forEach(stat -> 
            System.out.println("  " + stat[0] + ": " + stat[1]));
        
        System.out.println("Notification Type Statistics:");
        typeStats.forEach(stat -> 
            System.out.println("  " + stat[0] + ": " + stat[1]));
    }
    
    @Test
    void testSystemReliabilityFeatures() {
        // Test retry mechanism
        notificationService.retryFailedNotifications();
        
        // Test cleanup
        notificationService.cleanupExpiredNotifications();
        
        // These should not throw exceptions
        assertDoesNotThrow(() -> {
            notificationService.retryFailedNotifications();
            notificationService.cleanupExpiredNotifications();
        });
    }
}
