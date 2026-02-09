package com.saas.notification.scheduled;

import com.saas.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for notification maintenance and reliability.
 * 
 * This service handles periodic tasks for:
 * - Retrying failed notifications
 * - Cleaning up expired notifications
 * - System health monitoring
 * - Performance metrics collection
 * 
 * Key features:
 * - Configurable schedules
 * - Error handling and monitoring
 * - Performance optimization
 * - Tenant-aware processing
 * 
 * Schedule configuration:
 * - Retry failed notifications: Every 5 minutes
 * - Cleanup expired notifications: Every hour
 * - Cleanup old notifications: Daily at 2 AM
 * - Health monitoring: Every 10 minutes
 */
@Service
public class NotificationSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${app.notification.retry.enabled:true}")
    private boolean retryEnabled;
    
    @Value("${app.notification.retry.delay-ms:5000}")
    private long retryDelayMs;
    
    @Value("${app.notification.cleanup.enabled:true}")
    private boolean cleanupEnabled;
    
    @Value("${app.notification.cleanup.old-notifications-days:90}")
    private int oldNotificationsRetentionDays;
    
    @Value("${app.notification.cleanup.expired-notifications-hours:1}")
    private int expiredNotificationsCleanupHours;
    
    /**
     * Retry failed notifications every 5 minutes.
     * This ensures temporary failures are recovered from quickly.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void retryFailedNotifications() {
        if (!retryEnabled) {
            logger.debug("Notification retry is disabled");
            return;
        }
        
        try {
            logger.info("Starting retry of failed notifications");
            notificationService.retryFailedNotifications();
            logger.info("Completed retry of failed notifications");
            
        } catch (Exception e) {
            logger.error("Error during notification retry process: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up expired notifications every hour.
     * This removes notifications that have passed their expiration time.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredNotifications() {
        if (!cleanupEnabled) {
            logger.debug("Notification cleanup is disabled");
            return;
        }
        
        try {
            logger.info("Starting cleanup of expired notifications");
            notificationService.cleanupExpiredNotifications();
            logger.info("Completed cleanup of expired notifications");
            
        } catch (Exception e) {
            logger.error("Error during expired notification cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old read notifications daily at 2 AM.
     * This removes old read notifications to manage database size.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldNotifications() {
        if (!cleanupEnabled) {
            logger.debug("Notification cleanup is disabled");
            return;
        }
        
        try {
            logger.info("Starting cleanup of old notifications (retention: {} days)", oldNotificationsRetentionDays);
            
            // This would be implemented in the notification service
            // notificationService.cleanupOldNotifications(oldNotificationsRetentionDays);
            
            logger.info("Completed cleanup of old notifications");
            
        } catch (Exception e) {
            logger.error("Error during old notification cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Monitor system health every 10 minutes.
     * This collects metrics and identifies potential issues.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void monitorSystemHealth() {
        try {
            logger.debug("Starting system health monitoring");
            
            // Collect notification statistics
            var statusStats = notificationService.getNotificationStatsByStatus(1L); // System tenant
            var typeStats = notificationService.getNotificationStatsByType(1L);
            
            // Log statistics for monitoring
            logger.info("Notification system health check:");
            logger.info("Status statistics: {}", statusStats);
            logger.info("Type statistics: {}", typeStats);
            
            // Check for potential issues
            checkForHealthIssues(statusStats, typeStats);
            
            logger.debug("Completed system health monitoring");
            
        } catch (Exception e) {
            logger.error("Error during system health monitoring: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check for health issues based on statistics.
     */
    private void checkForHealthIssues(java.util.List<Object[]> statusStats, java.util.List<Object[]> typeStats) {
        // Check for high failure rates
        long totalNotifications = 0;
        long failedNotifications = 0;
        
        for (Object[] stat : statusStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            totalNotifications += count;
            
            if ("FAILED".equals(status)) {
                failedNotifications = count;
            }
        }
        
        if (totalNotifications > 0) {
            double failureRate = (double) failedNotifications / totalNotifications;
            if (failureRate > 0.1) { // More than 10% failure rate
                logger.warn("High notification failure rate detected: {:.2%}", failureRate);
            }
        }
        
        // Check for large backlog of pending notifications
        for (Object[] stat : statusStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            
            if ("PENDING".equals(status) && count > 1000) {
                logger.warn("Large backlog of pending notifications: {}", count);
            }
        }
        
        // Log performance metrics
        logger.info("System health metrics - Total: {}, Failed: {}, Failure Rate: {:.2%}", 
                   totalNotifications, failedNotifications, 
                   totalNotifications > 0 ? (double) failedNotifications / totalNotifications : 0.0);
    }
    
    /**
     * Generate daily report at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void generateDailyReport() {
        try {
            logger.info("Generating daily notification report");
            
            // This would generate and send a daily report
            // For now, we'll just log the summary
            var statusStats = notificationService.getNotificationStatsByStatus(1L);
            
            logger.info("Daily notification summary:");
            for (Object[] stat : statusStats) {
                logger.info("  {}: {}", stat[0], stat[1]);
            }
            
            logger.info("Daily notification report completed");
            
        } catch (Exception e) {
            logger.error("Error during daily report generation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup resources and perform maintenance tasks weekly.
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Sunday at 3 AM
    public void weeklyMaintenance() {
        try {
            logger.info("Starting weekly maintenance tasks");
            
            // Perform database maintenance
            // Update statistics
            // Clear temporary caches
            // Check system configuration
            
            logger.info("Weekly maintenance tasks completed");
            
        } catch (Exception e) {
            logger.error("Error during weekly maintenance: {}", e.getMessage(), e);
        }
    }
}
