package com.saas.notification.repository;

import com.saas.notification.entity.Notification;
import com.saas.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Notification entities.
 * 
 * Provides optimized queries for notification management with tenant isolation.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find notifications by tenant ID and user ID with pagination.
     */
    Page<Notification> findByTenantIdAndUserId(Long tenantId, UUID userId, Pageable pageable);
    
    /**
     * Find notifications by tenant ID, user ID, and read status.
     */
    Page<Notification> findByTenantIdAndUserIdAndIsRead(Long tenantId, UUID userId, Boolean isRead, Pageable pageable);
    
    /**
     * Find unread notifications for a user.
     */
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("tenantId") Long tenantId, @Param("userId") UUID userId);
    
    /**
     * Count unread notifications for a user.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId AND n.userId = :userId AND n.isRead = false")
    long countUnreadNotifications(@Param("tenantId") Long tenantId, @Param("userId") UUID userId);
    
    /**
     * Find notifications by status for retry processing.
     */
    @Query("SELECT n FROM Notification n WHERE n.status IN :statuses AND n.retryCount < n.maxRetries ORDER BY n.priority.level ASC, n.createdAt ASC")
    List<Notification> findNotificationsForRetry(@Param("statuses") List<NotificationStatus> statuses);
    
    /**
     * Find expired notifications.
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Mark notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.tenantId = :tenantId AND n.userId = :userId AND n.id IN :notificationIds")
    int markAsRead(@Param("tenantId") Long tenantId, @Param("userId") UUID userId, 
                   @Param("notificationIds") List<UUID> notificationIds, @Param("now") LocalDateTime now);
    
    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.tenantId = :tenantId AND n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("tenantId") Long tenantId, @Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Delete old read notifications to cleanup database.
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.tenantId = :tenantId AND n.isRead = true AND n.readAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("tenantId") Long tenantId, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find notifications by event ID for idempotency.
     */
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.eventId = :eventId")
    List<Notification> findByTenantIdAndEventId(@Param("tenantId") Long tenantId, @Param("eventId") String eventId);
    
    /**
     * Find notifications by entity type and ID.
     */
    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.entityType = :entityType AND n.entityId = :entityId ORDER BY n.createdAt DESC")
    List<Notification> findByTenantIdAndEntity(@Param("tenantId") Long tenantId, 
                                               @Param("entityType") String entityType, 
                                               @Param("entityId") String entityId);
    
    /**
     * Get notification statistics by status for a tenant.
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId GROUP BY n.status")
    List<Object[]> getNotificationStatsByStatus(@Param("tenantId") Long tenantId);
    
    /**
     * Get notification statistics by type for a tenant.
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.tenantId = :tenantId GROUP BY n.type")
    List<Object[]> getNotificationStatsByType(@Param("tenantId") Long tenantId);
}
