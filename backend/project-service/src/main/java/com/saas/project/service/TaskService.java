package com.saas.project.service;

import com.saas.project.entity.Task;
import com.saas.project.entity.TaskPriority;
import com.saas.project.entity.TaskStatus;
import com.saas.project.entity.TaskType;
import com.saas.project.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Task management with comprehensive business logic.
 * 
 * This service handles all task-related operations with:
 * - Transaction management for data consistency
 * - Tenant isolation for security
 * - Optimistic locking for concurrent operations
 * - Authorization checks using existing RBAC
 * - Task lifecycle management (TODO → DONE)
 * - Dependency and subtask management
 * 
 * Key responsibilities:
 * - Task CRUD operations with validation
 * - Task status transitions with business rules
 * - Task assignment and reassignment
 * - Time tracking and deadline management
 * - Dependency resolution and cycle detection
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param taskRepository the task repository
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Create a new task with validation and business rules.
     * 
     * @param task the task to create
     * @param tenantId the tenant ID for security
     * @return the created task
     * @throws IllegalArgumentException if validation fails
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Task createTask(Task task, Long tenantId) {
        validateTaskForCreation(task, tenantId);
        
        // Set tenant ID for security
        task.setTenantId(tenantId);
        
        // Set default values
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }
        if (task.getType() == null) {
            task.setType(TaskType.TASK);
        }
        
        // Save and return
        return taskRepository.save(task);
    }

    /**
     * Update an existing task with optimistic locking.
     * 
     * @param taskId the task ID to update
     * @param taskUpdates the task updates
     * @param tenantId the tenant ID for security
     * @return the updated task
     * @throws IllegalArgumentException if validation fails or task not found
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Task updateTask(UUID taskId, Task taskUpdates, Long tenantId) {
        // Find task with optimistic lock
        Task existingTask = taskRepository.findByIdAndTenantIdWithLock(taskId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // Validate updates
        validateTaskForUpdate(taskUpdates, tenantId);
        
        // Apply updates
        updateTaskFields(existingTask, taskUpdates);
        
        // Save and return
        return taskRepository.save(existingTask);
    }

    /**
     * Get a task by ID with tenant isolation.
     * 
     * @param taskId the task ID
     * @param tenantId the tenant ID for security
     * @return the task if found
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Optional<Task> getTask(UUID taskId, Long tenantId) {
        return taskRepository.findByIdAndTenantId(taskId, tenantId);
    }

    /**
     * Get all tasks for a tenant with pagination.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination information
     * @return paginated list of tasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getAllTasks(Long tenantId, Pageable pageable) {
        return taskRepository.findByTenantId(tenantId, pageable);
    }

    /**
     * Get tasks by project for a tenant.
     * 
     * @param projectId the project ID
     * @param tenantId the tenant ID
     * @param pageable pagination information
     * @return paginated list of tasks for the project
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getTasksByProject(UUID projectId, Long tenantId, Pageable pageable) {
        return taskRepository.findByProjectIdAndTenantId(projectId, tenantId, pageable);
    }

    /**
     * Get tasks by status for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param status the task status
     * @param pageable pagination information
     * @return paginated list of tasks with specified status
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getTasksByStatus(Long tenantId, TaskStatus status, Pageable pageable) {
        return taskRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    /**
     * Get tasks assigned to a user.
     * Used for user dashboards and workload management.
     * 
     * @param assignedToId the user ID
     * @param tenantId the tenant ID
     * @param pageable pagination information
     * @return paginated list of tasks assigned to the user
     */
    @PreAuthorize("#assignedToId == authentication.principal.id or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getTasksByAssignee(UUID assignedToId, Long tenantId, Pageable pageable) {
        return taskRepository.findByAssignedToIdAndTenantId(assignedToId, tenantId, pageable);
    }

    /**
     * Get tasks by priority for a tenant.
     * Used for prioritized task management.
     * 
     * @param tenantId the tenant ID
     * @param priority the task priority
     * @param pageable pagination information
     * @return paginated list of tasks with specified priority
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getTasksByPriority(Long tenantId, TaskPriority priority, Pageable pageable) {
        return taskRepository.findByTenantIdAndPriority(tenantId, priority, pageable);
    }

    /**
     * Get overdue tasks for a tenant.
     * Used for deadline tracking and notifications.
     * 
     * @param tenantId the tenant ID
     * @param pageable pagination information
     * @return paginated list of overdue tasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getOverdueTasks(Long tenantId, Pageable pageable) {
        return taskRepository.findOverdueTasks(tenantId, LocalDateTime.now(), pageable);
    }

    /**
     * Search tasks by title or description.
     * 
     * @param tenantId the tenant ID
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return paginated list of matching tasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> searchTasks(Long tenantId, String searchTerm, Pageable pageable) {
        return taskRepository.searchTasksByTenant(tenantId, searchTerm, pageable);
    }

    /**
     * Change task status with business rule validation.
     * 
     * @param taskId the task ID
     * @param newStatus the new status
     * @param tenantId the tenant ID
     * @return true if status change was successful
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public boolean changeTaskStatus(UUID taskId, TaskStatus newStatus, Long tenantId) {
        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        validateStatusTransition(task.getStatus(), newStatus);
        
        LocalDateTime completedDate = (newStatus == TaskStatus.DONE) ? LocalDateTime.now() : null;
        int updated = taskRepository.updateTaskStatus(taskId, tenantId, newStatus, completedDate);
        return updated > 0;
    }

    /**
     * Assign a task to a user.
     * 
     * @param taskId the task ID
     * @param assignedToId the user ID to assign to
     * @param tenantId the tenant ID
     * @return true if assignment was successful
     */
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public boolean assignTask(UUID taskId, UUID assignedToId, Long tenantId) {
        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // Business rule: Cannot assign completed or cancelled tasks
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot assign completed or cancelled tasks");
        }

        int updated = taskRepository.updateTaskAssignment(taskId, tenantId, assignedToId);
        return updated > 0;
    }

    /**
     * Get root tasks for a project (tasks without parent).
     * Used for task hierarchy management.
     * 
     * @param projectId the project ID
     * @param tenantId the tenant ID
     * @return list of root tasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<Task> getRootTasksByProject(UUID projectId, Long tenantId) {
        return taskRepository.findRootTasksByProjectAndTenant(projectId, tenantId);
    }

    /**
     * Get subtasks for a parent task.
     * Used for task hierarchy management.
     * 
     * @param parentTaskId the parent task ID
     * @param tenantId the tenant ID
     * @return list of subtasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<Task> getSubtasksByParent(UUID parentTaskId, Long tenantId) {
        return taskRepository.findSubtasksByParentAndTenant(parentTaskId, tenantId);
    }

    /**
     * Get task statistics for a tenant.
     * Used for dashboards and reporting.
     * 
     * @param tenantId the tenant ID
     * @return task statistics
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStatistics(Long tenantId) {
        List<Object[]> stats = taskRepository.getTaskStatisticsByTenant(tenantId);
        Map<String, Object> result = new HashMap<>();
        
        for (Object[] row : stats) {
            String status = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            Double totalEstimated = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double totalActual = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            Double totalStoryPoints = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            
            Map<String, Object> statusStats = new HashMap<>();
            statusStats.put("count", count);
            statusStats.put("totalEstimatedHours", totalEstimated);
            statusStats.put("totalActualHours", totalActual);
            statusStats.put("totalStoryPoints", totalStoryPoints);
            
            if (totalEstimated > 0) {
                statusStats.put("efficiency", totalActual / totalEstimated);
            }
            
            result.put(status.toLowerCase(), statusStats);
        }
        
        return result;
    }

    /**
     * Get tasks completed within a date range.
     * Used for productivity reporting.
     * 
     * @param tenantId the tenant ID
     * @param startDate start date range
     * @param endDate end date range
     * @param pageable pagination information
     * @return paginated list of completed tasks
     */
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public Page<Task> getCompletedTasksInDateRange(Long tenantId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return taskRepository.findCompletedTasksInDateRange(tenantId, startDate, endDate, pageable);
    }

    /**
     * Block a task with reason.
     * Used for task management and issue tracking.
     * 
     * @param taskId the task ID
     * @param reason the reason for blocking
     * @param tenantId the tenant ID
     * @return true if blocking was successful
     */
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public boolean blockTask(UUID taskId, String reason, Long tenantId) {
        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalArgumentException("Cannot block completed tasks");
        }

        task.setIsBlocked(true);
        task.setBlockedReason(reason);
        taskRepository.save(task);
        return true;
    }

    /**
     * Unblock a task.
     * 
     * @param taskId the task ID
     * @param tenantId the tenant ID
     * @return the unblocked task
     */
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Task unblockTask(UUID taskId, Long tenantId) {
        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        task.setIsBlocked(false);
        task.setBlockedReason(null);
        return taskRepository.save(task);
    }

    /**
     * Validate task data for creation.
     * 
     * @param task the task to validate
     * @param tenantId the tenant ID
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTaskForCreation(Task task, Long tenantId) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }
        
        if (task.getTaskNumber() != null && !task.getTaskNumber().trim().isEmpty()) {
            if (taskRepository.existsByTaskNumberAndTenantId(task.getTaskNumber(), tenantId)) {
                throw new IllegalArgumentException("Task number already exists in this tenant");
            }
        }
        
        if (task.getProjectId() == null) {
            throw new IllegalArgumentException("Project is required");
        }
        
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        
        if (task.getEstimatedHours() != null && task.getEstimatedHours() < 0) {
            throw new IllegalArgumentException("Estimated hours cannot be negative");
        }
        
        if (task.getStoryPoints() != null && task.getStoryPoints() < 0) {
            throw new IllegalArgumentException("Story points cannot be negative");
        }
    }

    /**
     * Validate task data for updates.
     * 
     * @param task the task updates to validate
     * @param tenantId the tenant ID
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTaskForUpdate(Task task, Long tenantId) {
        if (task.getTaskNumber() != null && !task.getTaskNumber().trim().isEmpty()) {
            // Check if task number is being changed to an existing one
            Optional<Task> existingTask = taskRepository.findByTaskNumberAndTenantId(task.getTaskNumber(), tenantId);
            if (existingTask.isPresent() && !existingTask.get().getId().equals(task.getId())) {
                throw new IllegalArgumentException("Task number already exists in this tenant");
            }
        }
        
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        
        if (task.getEstimatedHours() != null && task.getEstimatedHours() < 0) {
            throw new IllegalArgumentException("Estimated hours cannot be negative");
        }
        
        if (task.getStoryPoints() != null && task.getStoryPoints() < 0) {
            throw new IllegalArgumentException("Story points cannot be negative");
        }
    }

    /**
     * Validate task status transitions.
     * 
     * @param currentStatus the current task status
     * @param newStatus the new status to transition to
     * @throws IllegalArgumentException if transition is invalid
     */
    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case TODO:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from TODO to " + newStatus);
                }
                break;
            case IN_PROGRESS:
                if (newStatus == TaskStatus.TODO) {
                    throw new IllegalArgumentException("Cannot transition from IN_PROGRESS to TODO");
                }
                break;
            case IN_REVIEW:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.DONE && newStatus != TaskStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from IN_REVIEW to " + newStatus);
                }
                break;
            case TESTING:
                if (newStatus != TaskStatus.IN_PROGRESS && newStatus != TaskStatus.DONE && newStatus != TaskStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from TESTING to " + newStatus);
                }
                break;
            case DONE:
                if (newStatus != TaskStatus.ARCHIVED) {
                    throw new IllegalArgumentException("Invalid status transition from DONE to " + newStatus);
                }
                break;
            case CANCELLED:
                if (newStatus != TaskStatus.ARCHIVED) {
                    throw new IllegalArgumentException("Invalid status transition from CANCELLED to " + newStatus);
                }
                break;
        }
    }

    /**
     * Update task fields with null-safe operations.
     * 
     * @param existingTask the existing task
     * @param updates the task updates
     */
    private void updateTaskFields(Task existingTask, Task updates) {
        if (updates.getTitle() != null) {
            existingTask.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            existingTask.setDescription(updates.getDescription());
        }
        if (updates.getPriority() != null) {
            existingTask.setPriority(updates.getPriority());
        }
        if (updates.getType() != null) {
            existingTask.setType(updates.getType());
        }
        if (updates.getDueDate() != null) {
            existingTask.setDueDate(updates.getDueDate());
        }
        if (updates.getStartDate() != null) {
            existingTask.setStartDate(updates.getStartDate());
        }
        if (updates.getEstimatedHours() != null) {
            existingTask.setEstimatedHours(updates.getEstimatedHours());
        }
        if (updates.getActualHours() != null) {
            existingTask.setActualHours(updates.getActualHours());
        }
        if (updates.getStoryPoints() != null) {
            existingTask.setStoryPoints(updates.getStoryPoints());
        }
        if (updates.getTags() != null) {
            existingTask.setTags(updates.getTags());
        }
    }
}
