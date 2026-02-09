package com.saas.notification.service;

import com.saas.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending real-time notifications via WebSocket.
 * 
 * This service handles WebSocket-based notifications with:
 * - Tenant-isolated channels
 * - User-specific targeting
 * - Real-time delivery
 * - Connection management
 * 
 * WebSocket endpoints:
 * - /user/{userId}/notifications - User-specific notifications
 * - /tenant/{tenantId}/notifications - Tenant-wide notifications
 * - /notifications - Global system notifications
 * 
 * In a production environment, this would include:
 * - Connection authentication and authorization
 * - Load balancing across multiple instances
 * - Message acknowledgments and retries
 * - Rate limiting and spam protection
 */
@Service
public class WebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send a real-time notification to a specific user.
     * 
     * @param notification the notification to send
     * @return true if successful, false otherwise
     */
    @SuppressWarnings("null")
    public boolean sendRealTimeNotification(Notification notification) {
        try {
            // Create WebSocket message payload
            Map<String, Object> payload = createWebSocketPayload(notification);
            
            // Send to user-specific channel
            String userDestination = String.format("/user/%s/notifications", notification.getUserId());
            messagingTemplate.convertAndSend(userDestination, payload);
            
            // Also send to tenant channel for admin monitoring
            String tenantDestination = String.format("/tenant/%s/notifications", notification.getTenantId());
            messagingTemplate.convertAndSend(tenantDestination, payload);
            
            logger.info("WebSocket notification sent to user: {} in tenant: {} for notification: {}", 
                       notification.getUserId(), notification.getTenantId(), notification.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to user: {} for notification: {} - {}", 
                        notification.getUserId(), notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send a broadcast notification to all users in a tenant.
     * 
     * @param tenantId the tenant ID
     * @param notification the notification to broadcast
     * @return true if successful, false otherwise
     */
    @SuppressWarnings("null")
    public boolean broadcastToTenant(Long tenantId, Notification notification) {
        try {
            Map<String, Object> payload = createWebSocketPayload(notification);
            payload.put("broadcast", true);
            payload.put("targetType", "TENANT");
            
            String destination = String.format("/tenant/%s/broadcast", tenantId);
            messagingTemplate.convertAndSend(destination, payload);
            
            logger.info("WebSocket broadcast sent to tenant: {} for notification: {}", 
                       tenantId, notification.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket broadcast to tenant: {} for notification: {} - {}", 
                        tenantId, notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send a system-wide notification.
     * 
     * @param notification the notification to broadcast
     * @return true if successful, false otherwise
     */
    public boolean broadcastSystemNotification(Notification notification) {
        try {
            Map<String, Object> payload = createWebSocketPayload(notification);
            payload.put("broadcast", true);
            payload.put("targetType", "SYSTEM");
            
            String destination = "/system/notifications";
            messagingTemplate.convertAndSend(destination, payload);
            
            logger.info("System WebSocket broadcast sent for notification: {}", notification.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send system WebSocket broadcast for notification: {} - {}", 
                        notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create a WebSocket message payload from a notification.
     */
    private Map<String, Object> createWebSocketPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("id", notification.getId());
        payload.put("type", notification.getType().name());
        payload.put("title", notification.getTitle());
        payload.put("message", notification.getMessage());
        payload.put("priority", notification.getPriority().name());
        payload.put("source", notification.getSource());
        payload.put("entityType", notification.getEntityType());
        payload.put("entityId", notification.getEntityId());
        payload.put("actionUrl", notification.getActionUrl());
        payload.put("timestamp", notification.getCreatedAt());
        payload.put("tenantId", notification.getTenantId());
        payload.put("userId", notification.getUserId());
        payload.put("userEmail", notification.getUserEmail());
        
        // Add display-friendly fields
        payload.put("typeDisplay", notification.getType().getDisplayName());
        payload.put("priorityDisplay", notification.getPriority().getDisplayName());
        payload.put("isUrgent", notification.getPriority().isUrgent());
        
        // Add time-based fields
        payload.put("timeAgo", getTimeAgo(notification.getCreatedAt()));
        payload.put("isRead", notification.getIsRead());
        
        return payload;
    }
    
    /**
     * Send a notification status update (e.g., marked as read).
     */
    @SuppressWarnings("null")
    public boolean sendNotificationUpdate(Long tenantId, UUID userId, UUID notificationId, String status) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "NOTIFICATION_UPDATE");
            payload.put("notificationId", notificationId);
            payload.put("status", status);
            payload.put("timestamp", LocalDateTime.now());
            payload.put("userId", userId);
            payload.put("tenantId", tenantId);
            
            String userDestination = String.format("/user/%s/updates", userId);
            messagingTemplate.convertAndSend(userDestination, payload);
            
            logger.info("WebSocket notification update sent to user: {} for notification: {} with status: {}", 
                       userId, notificationId, status);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification update to user: {} for notification: {} - {}", 
                        userId, notificationId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send a connection status message.
     */
    @SuppressWarnings("null")
    public void sendConnectionStatus(Long tenantId, UUID userId, String status) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "CONNECTION_STATUS");
            payload.put("status", status);
            payload.put("userId", userId);
            payload.put("tenantId", tenantId);
            payload.put("timestamp", LocalDateTime.now());
            
            String destination = String.format("/tenant/%s/connections", tenantId);
            messagingTemplate.convertAndSend(destination, payload);
            
            logger.info("WebSocket connection status sent to tenant: {} for user: {} with status: {}", 
                       tenantId, userId, status);
            
        } catch (Exception e) {
            logger.error("Failed to send WebSocket connection status to tenant: {} for user: {} - {}", 
                        tenantId, userId, e.getMessage(), e);
        }
    }
    
    /**
     * Get a human-readable time ago string.
     * This is a simplified implementation - in production, use a proper time library.
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else {
            long hours = minutes / 60;
            if (hours < 24) {
                return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
            } else {
                long days = hours / 24;
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            }
        }
    }
}
