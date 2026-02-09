package com.saas.project.controller;

import com.saas.project.entity.Task;
import com.saas.project.dto.TaskRequest;
import com.saas.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Task management operations.
 * 
 * This controller provides REST endpoints for task management with:
 * - Comprehensive validation using Jakarta Bean Validation
 * - Role-based access control using existing RBAC
 * - Tenant isolation for security
 * - OpenAPI documentation for API discovery
 * - Proper HTTP status codes and error handling
 * 
 * Key features:
 * - All operations are tenant-aware via X-Tenant-ID header
 * - Optimistic locking prevents concurrent modification conflicts
 * - Business rule enforcement in service layer
 * - Task lifecycle management (TODO → DONE)
 * - Assignment and dependency management
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    /**
     * Constructor for dependency injection.
     * 
     * @param taskService the task service
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create a new task.
     * 
     * @param taskRequest the task creation request
     * @param tenantId the tenant ID from header
     * @return the created task
     */
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
                content = @Content(schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody TaskRequest taskRequest,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Task createdTask = taskService.createTask(convertToTask(taskRequest), tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * Get a task by ID.
     * 
     * @param id the task ID
     * @param tenantId the tenant ID from header
     * @return the task if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a task by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task found",
                content = @Content(schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Task> getTask(
            @Parameter(description = "Task ID", required = true) @PathVariable UUID id,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        return taskService.getTask(id, tenantId)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all tasks with pagination.
     * 
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of tasks
     */
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves a paginated list of tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Task>> getAllTasks(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Task> tasks = taskService.getAllTasks(tenantId, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get tasks by project.
     * 
     * @param projectId the project ID
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of tasks for the project
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project", description = "Retrieves tasks for a specific project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Task>> getTasksByProject(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID projectId,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Task> tasks = taskService.getTasksByProject(projectId, tenantId, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Update an existing task.
     * 
     * @param id the task ID
     * @param taskRequest the task update request
     * @param tenantId the tenant ID from header
     * @return the updated task
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates an existing task with provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
                content = @Content(schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "Task ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody TaskRequest taskRequest,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Task updatedTask = taskService.updateTask(id, convertToTask(taskRequest), tenantId);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Change task status.
     * 
     * @param id the task ID
     * @param newStatus the new status
     * @param tenantId the tenant ID from header
     * @return success response
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change task status", description = "Changes the status of an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> changeTaskStatus(
            @Parameter(description = "Task ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New status", required = true) @RequestBody com.saas.project.entity.TaskStatus newStatus,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        boolean success = taskService.changeTaskStatus(id, newStatus, tenantId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Task status updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update task status"));
        }
    }

    /**
     * Assign task to user.
     * 
     * @param id the task ID
     * @param assignedToId the user ID to assign to
     * @param tenantId the tenant ID from header
     * @return success response
     */
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign task", description = "Assigns a task to a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid assignment"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> assignTask(
            @Parameter(description = "Task ID", required = true) @PathVariable UUID id,
            @Parameter(description = "User ID to assign to", required = true) @RequestBody UUID assignedToId,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        boolean success = taskService.assignTask(id, assignedToId, tenantId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Task assigned successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to assign task"));
        }
    }

    /**
     * Get overdue tasks.
     * 
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of overdue tasks
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieves tasks that are past their due date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Task>> getOverdueTasks(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: dueDate)") @RequestParam(defaultValue = "dueDate") String sort,
            @Parameter(description = "Sort direction (default: asc)") @RequestParam(defaultValue = "asc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Task> tasks = taskService.getOverdueTasks(tenantId, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get task statistics.
     * 
     * @param tenantId the tenant ID from header
     * @return task statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get task statistics", description = "Retrieves task statistics for the tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskStatistics(
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Map<String, Object> statistics = taskService.getTaskStatistics(tenantId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Search tasks.
     * 
     * @param searchTerm the search term
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of matching tasks
     */
    @GetMapping("/search")
    @Operation(summary = "Search tasks", description = "Searches tasks by title or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Task>> searchTasks(
            @Parameter(description = "Search term", required = true) @RequestParam String searchTerm,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Task> tasks = taskService.searchTasks(tenantId, searchTerm, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Convert TaskRequest to Task entity.
     * 
     * @param request the DTO to convert
     * @return Task entity
     */
    private Task convertToTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskNumber(request.getTaskNumber());
        task.setPriority(request.getPriority());
        task.setType(request.getType());
        task.setDueDate(request.getDueDate());
        task.setStartDate(request.getStartDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStoryPoints(request.getStoryPoints());
        task.setTags(request.getTags());
        return task;
    }

    /**
     * Custom ResponseEntity for 404 responses.
     * 
     * @return ResponseEntity with 404 status
     */
    private ResponseEntity<Map<String, String>> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Resource not found"));
    }
}
