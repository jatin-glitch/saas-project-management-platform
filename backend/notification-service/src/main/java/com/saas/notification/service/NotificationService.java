package com.saas.notification.service;

import com.saas.notification.entity.Notification;
import com.saas.notification.entity.NotificationPriority;
import com.saas.notification.entity.NotificationStatus;
import com.saas.notification.entity.NotificationType;
import com.saas.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing notifications with multi-tenant support.
 * 
 * This service handles the complete lifecycle of notifications:
 * - Creation from domain events
 * - Delivery via multiple channels
 * - Read/unread status management
 * - Cleanup and maintenance
 * 
 * Key features:
 * - Tenant isolation for security
 * - Idempotent processing to prevent duplicates
 * - Retry logic for failed deliveries
 * - Performance optimization with pagination
 */
@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    
    /**
     * Create a notification from a domain event.
     * 
     * @param tenantId the tenant ID
     * @param userId the user ID to notify
     * @param userEmail the user email
     * @param type the notification type
     * @param title the notification title
     * @param message the notification message
     * @param source the source of the notification
     * @param eventId the event ID for idempotency
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param actionUrl the action URL
     * @param deliveryChannels comma-separated delivery channels
     * @param priority the notification priority
     * @return the created notification
     */
    public Notification createNotification(Long tenantId, UUID userId, String userEmail,
                                         NotificationType type, String title, String message,
                                         String source, String eventId, String entityType, String entityId,
                                         String actionUrl, String deliveryChannels, NotificationPriority priority) {
        
        // Check for idempotency - don't create duplicate notifications for the same event
        if (eventId != null) {
            List<Notification> existingNotifications = notificationRepository.findByTenantIdAndEventId(tenantId, eventId);
            if (!existingNotifications.isEmpty()) {
                logger.info("Notification already exists for event ID: {}, skipping creation", eventId);
                return existingNotifications.get(0);
            }
        }
        
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setUserId(userId);
        notification.setUserEmail(userEmail);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSource(source);
        notification.setEventId(eventId);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setActionUrl(actionUrl);
        notification.setDeliveryChannels(deliveryChannels);
        notification.setPriority(priority != null ? priority : NotificationPriority.MEDIUM);
        
        // Set expiration for non-critical notifications (30 days)
        if (notification.getPriority() != NotificationPriority.CRITICAL) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
        }
        
        notification = notificationRepository.save(notification);
        logger.info("Created notification: {} for user: {} in tenant: {}", 
                   notification.getId(), userId, tenantId);
        
        // Process notification asynchronously
        processNotification(notification);
        
        return notification;
    }
    
    /**
     * Process a notification through all configured delivery channels.
     */
    @Transactional
    public void processNotification(Notification notification) {
        try {
            notification.setStatus(NotificationStatus.PROCESSING);
            notificationRepository.save(notification);
            
            String deliveryChannels = notification.getDeliveryChannels();
            if (deliveryChannels == null || deliveryChannels.isEmpty()) {
                deliveryChannels = "IN_APP"; // Default to in-app notifications
            }
            
            List<String> channels = Arrays.asList(deliveryChannels.split(","));
            boolean overallSuccess = true;
            
            for (String channel : channels) {
                try {
                    boolean success = processNotificationChannel(notification, channel.trim().toUpperCase());
                    if (!success) {
                        overallSuccess = false;
                    }
                } catch (Exception e) {
                    logger.error("Error processing notification channel: {} for notification: {}", 
                               channel, notification.getId(), e);
                    overallSuccess = false;
                }
            }
            
            // Update notification status based on overall success
            if (overallSuccess) {
                notification.setStatus(NotificationStatus.DELIVERED);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.incrementRetryCount();
            }
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            logger.error("Error processing notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.incrementRetryCount();
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Process notification for a specific channel.
     */
    private boolean processNotificationChannel(Notification notification, String channel) {
        switch (channel) {
            case "IN_APP":
                // In-app notifications are stored in the database, so this is always successful
                return true;
                
            case "EMAIL":
                return emailNotificationService.sendEmail(notification);
                
            case "WEBSOCKET":
                return webSocketNotificationService.sendRealTimeNotification(notification);
                
            default:
                logger.warn("Unknown notification channel: {}", channel);
                return false;
        }
    }
    
    /**
     * Get notifications for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long tenantId, UUID userId, Pageable pageable) {
        return notificationRepository.findByTenantIdAndUserId(tenantId, userId, pageable);
    }
    
    /**
     * Get unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long tenantId, UUID userId) {
        return notificationRepository.findUnreadNotifications(tenantId, userId);
    }
    
    /**
     * Count unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long tenantId, UUID userId) {
        return notificationRepository.countUnreadNotifications(tenantId, userId);
    }
    
    /**
     * Mark notifications as read.
     */
    public int markAsRead(Long tenantId, UUID userId, List<UUID> notificationIds) {
        return notificationRepository.markAsRead(tenantId, userId, notificationIds, LocalDateTime.now());
    }
    
    /**
     * Mark all notifications as read for a user.
     */
    public int markAllAsRead(Long tenantId, UUID userId) {
        return notificationRepository.markAllAsRead(tenantId, userId, LocalDateTime.now());
    }
    
    /**
     * Retry failed notifications.
     */
    @Transactional
    public void retryFailedNotifications() {
        List<NotificationStatus> statusesToRetry = Arrays.asList(
            NotificationStatus.FAILED, 
            NotificationStatus.PENDING
        );
        
        List<Notification> notificationsToRetry = notificationRepository.findNotificationsForRetry(statusesToRetry);
        
        for (Notification notification : notificationsToRetry) {
            if (notification.canRetry() && !notification.isExpired()) {
                logger.info("Retrying notification: {} for user: {}", notification.getId(), notification.getUserId());
                processNotification(notification);
            }
        }
    }
    
    /**
     * Clean up expired notifications.
     */
    @Transactional
    public void cleanupExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        
        for (Notification notification : expiredNotifications) {
            notification.setStatus(NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
        }
        
        // Delete old read notifications (older than 90 days)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        int deletedCount = notificationRepository.deleteOldReadNotifications(1L, cutoffDate); // System tenant ID
        
        logger.info("Cleaned up {} old read notifications", deletedCount);
    }
    
    /**
     * Get notification statistics for a tenant.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNotificationStatsByStatus(Long tenantId) {
        return notificationRepository.getNotificationStatsByStatus(tenantId);
    }
    
    /**
     * Get notification statistics by type for a tenant.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getNotificationStatsByType(Long tenantId) {
        return notificationRepository.getNotificationStatsByType(tenantId);
    }
}
