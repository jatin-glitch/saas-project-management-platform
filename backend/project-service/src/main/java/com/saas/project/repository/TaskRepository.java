package com.saas.project.repository;

import com.saas.project.entity.Task;
import com.saas.project.entity.TaskPriority;
import com.saas.project.entity.TaskStatus;
import com.saas.project.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Task entity operations.
 * 
 * This repository provides tenant-aware task data access with:
 * - Optimistic locking for concurrent operations
 * - Tenant isolation for security
 * - Performance-optimized queries
 * - Support for complex task relationships
 * 
 * Key features:
 * - All operations are tenant-scoped for security
 * - Optimistic locking prevents concurrent modification conflicts
 * - Custom queries for task lifecycle management
 * - Support for dependencies and subtasks
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find task by ID and tenant ID.
     * Ensures tenant isolation for security.
     * 
     * @param id task ID
     * @param tenantId tenant ID
     * @return Optional containing the task if found
     */
    Optional<Task> findByIdAndTenantId(UUID id, Long tenantId);

    /**
     * Find task by number and tenant ID.
     * Used for unique task identification within project.
     * 
     * @param taskNumber task number
     * @param tenantId tenant ID
     * @return Optional containing the task if found
     */
    Optional<Task> findByTaskNumberAndTenantId(String taskNumber, Long tenantId);

    /**
     * Find all tasks for a specific tenant with pagination.
     * 
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of tasks
     */
    Page<Task> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Find tasks by project within tenant.
     * Used for project task management.
     * 
     * @param projectId project ID
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of tasks for the project
     */
    Page<Task> findByProjectIdAndTenantId(UUID projectId, Long tenantId, Pageable pageable);

    /**
     * Find tasks by status within tenant.
     * Used for task management dashboards.
     * 
     * @param tenantId tenant ID
     * @param status task status
     * @param pageable pagination information
     * @return Page of tasks with specified status
     */
    Page<Task> findByTenantIdAndStatus(Long tenantId, TaskStatus status, Pageable pageable);

    /**
     * Find tasks by assignee within tenant.
     * Used for user task dashboards.
     * 
     * @param assignedToId user ID
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of tasks assigned to the user
     */
    Page<Task> findByAssignedToIdAndTenantId(UUID assignedToId, Long tenantId, Pageable pageable);

    /**
     * Find tasks by reporter within tenant.
     * Used for user task reporting.
     * 
     * @param reporterId user ID
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of tasks reported by the user
     */
    Page<Task> findByReporterIdAndTenantId(UUID reporterId, Long tenantId, Pageable pageable);

    /**
     * Find tasks by priority within tenant.
     * Used for prioritized task management.
     * 
     * @param tenantId tenant ID
     * @param priority task priority
     * @param pageable pagination information
     * @return Page of tasks with specified priority
     */
    Page<Task> findByTenantIdAndPriority(Long tenantId, TaskPriority priority, Pageable pageable);

    /**
     * Find tasks due within a date range.
     * Used for deadline tracking and notifications.
     * 
     * @param tenantId tenant ID
     * @param startDate start date range
     * @param endDate end date range
     * @param pageable pagination information
     * @return Page of tasks due in the specified range
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId " +
           "AND t.dueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.dueDate ASC")
    Page<Task> findTasksDueInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find overdue tasks within tenant.
     * Used for overdue task tracking.
     * 
     * @param tenantId tenant ID
     * @param currentTime current time
     * @param pageable pagination information
     * @return Page of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId " +
           "AND t.dueDate < :currentTime " +
           "AND t.status NOT IN ('DONE', 'CANCELLED') " +
           "ORDER BY t.dueDate ASC")
    Page<Task> findOverdueTasks(
            @Param("tenantId") Long tenantId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    /**
     * Find tasks without parent (root tasks) within project.
     * Used for task hierarchy management.
     * 
     * @param projectId project ID
     * @param tenantId tenant ID
     * @return List of root tasks
     */
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId " +
           "AND t.tenantId = :tenantId " +
           "AND t.parentTaskId IS NULL " +
           "ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findRootTasksByProjectAndTenant(
            @Param("projectId") UUID projectId,
            @Param("tenantId") Long tenantId);

    /**
     * Find subtasks of a parent task.
     * Used for task hierarchy management.
     * 
     * @param parentTaskId parent task ID
     * @param tenantId tenant ID
     * @return List of subtasks
     */
    @Query("SELECT t FROM Task t WHERE t.parentTaskId = :parentTaskId " +
           "AND t.tenantId = :tenantId " +
           "ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findSubtasksByParentAndTenant(
            @Param("parentTaskId") UUID parentTaskId,
            @Param("tenantId") Long tenantId);

    /**
     * Search tasks by title or description within tenant.
     * Used for task search functionality.
     * 
     * @param tenantId tenant ID
     * @param searchTerm search term
     * @param pageable pagination information
     * @return Page of tasks matching the search term
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY t.priority DESC, t.createdAt DESC")
    Page<Task> searchTasksByTenant(
            @Param("tenantId") Long tenantId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Count tasks by status within tenant.
     * Used for dashboard statistics.
     * 
     * @param tenantId tenant ID
     * @param status task status
     * @return count of tasks with specified status
     */
    long countByTenantIdAndStatus(Long tenantId, TaskStatus status);

    /**
     * Count tasks by assignee within tenant.
     * Used for user workload statistics.
     * 
     * @param assignedToId user ID
     * @param tenantId tenant ID
     * @return count of tasks assigned to user
     */
    long countByAssignedToIdAndTenantId(UUID assignedToId, Long tenantId);

    /**
     * Find tasks with optimistic locking.
     * Used for concurrent updates to prevent lost updates.
     * 
     * @param id task ID
     * @param tenantId tenant ID
     * @return Optional containing the task with lock
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.tenantId = :tenantId")
    Optional<Task> findByIdAndTenantIdWithLock(
            @Param("id") UUID id, 
            @Param("tenantId") Long tenantId);

    /**
     * Update task status with tenant isolation.
     * Used for bulk status updates.
     * 
     * @param taskId task ID
     * @param tenantId tenant ID
     * @param newStatus new task status
     * @return number of updated tasks
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.completedDate = :completedDate " +
           "WHERE t.id = :taskId AND t.tenantId = :tenantId")
    int updateTaskStatus(
            @Param("taskId") UUID taskId,
            @Param("tenantId") Long tenantId,
            @Param("newStatus") TaskStatus newStatus,
            @Param("completedDate") LocalDateTime completedDate);

    /**
     * Update task assignment with tenant isolation.
     * Used for task reassignment.
     * 
     * @param taskId task ID
     * @param tenantId tenant ID
     * @param assignedToId new assignee user ID
     * @return number of updated tasks
     */
    @Modifying
    @Query("UPDATE Task t SET t.assignedToId = :assignedToId " +
           "WHERE t.id = :taskId AND t.tenantId = :tenantId")
    int updateTaskAssignment(
            @Param("taskId") UUID taskId,
            @Param("tenantId") Long tenantId,
            @Param("assignedToId") UUID assignedToId);

    /**
     * Find blocked tasks within tenant.
     * Used for task management and issue tracking.
     * 
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of blocked tasks
     */
    Page<Task> findByTenantIdAndIsBlocked(Long tenantId, boolean isBlocked, Pageable pageable);

    /**
     * Get task statistics for tenant.
     * Used for dashboard and reporting.
     * 
     * @param tenantId tenant ID
     * @return task statistics
     */
    @Query("SELECT " +
           "t.status as status, " +
           "COUNT(t) as count, " +
           "SUM(t.estimatedHours) as totalEstimatedHours, " +
           "SUM(t.actualHours) as totalActualHours, " +
           "SUM(t.storyPoints) as totalStoryPoints " +
           "FROM Task t " +
           "WHERE t.tenantId = :tenantId " +
           "GROUP BY t.status")
    List<Object[]> getTaskStatisticsByTenant(@Param("tenantId") Long tenantId);

    /**
     * Find tasks by type within tenant.
     * Used for task type filtering and reporting.
     * 
     * @param tenantId tenant ID
     * @param type task type
     * @param pageable pagination information
     * @return Page of tasks with specified type
     */
    Page<Task> findByTenantIdAndType(Long tenantId, TaskType type, Pageable pageable);

    /**
     * Find tasks completed within date range.
     * Used for productivity reporting.
     * 
     * @param tenantId tenant ID
     * @param startDate start date range
     * @param endDate end date range
     * @param pageable pagination information
     * @return Page of completed tasks in date range
     */
    @Query("SELECT t FROM Task t WHERE t.tenantId = :tenantId " +
           "AND t.status = 'DONE' " +
           "AND t.completedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.completedDate DESC")
    Page<Task> findCompletedTasksInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Check if task number exists within tenant.
     * Used for validation during task creation.
     * 
     * @param taskNumber task number
     * @param tenantId tenant ID
     * @return true if task number exists, false otherwise
     */
    boolean existsByTaskNumberAndTenantId(String taskNumber, Long tenantId);
}
