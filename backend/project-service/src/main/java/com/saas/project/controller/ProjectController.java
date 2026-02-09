package com.saas.project.controller;

import com.saas.project.entity.Project;
import com.saas.project.entity.ProjectPriority;
import com.saas.project.entity.ProjectStatus;
import com.saas.project.dto.ProjectRequest;
import com.saas.project.service.ProjectService;
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

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Project management operations.
 * 
 * This controller provides REST endpoints for project management with:
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
 * - Pagination support for large datasets
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project Management", description = "APIs for managing projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Constructor for dependency injection.
     * 
     * @param projectService the project service
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Create a new project.
     * 
     * @param projectRequest the project creation request
     * @param tenantId the tenant ID from header
     * @return the created project
     */
    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Project created successfully",
                content = @Content(schema = @Schema(implementation = Project.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Project> createProject(
            @Valid @RequestBody ProjectRequest projectRequest,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Project createdProject = projectService.createProject(convertToProject(projectRequest), tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * Get a project by ID.
     * 
     * @param id the project ID
     * @param tenantId the tenant ID from header
     * @return the project if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves a project by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project found",
                content = @Content(schema = @Schema(implementation = Project.class))),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Project> getProject(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID id,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        return projectService.getProject(id, tenantId)
                .map(project -> ResponseEntity.ok(project))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all projects with pagination.
     * 
     * @param page the page number (default: 0)
     * @param size the page size (default: 20)
     * @param sort the sort field (default: "createdAt")
     * @param direction the sort direction (default: "desc")
     * @param tenantId the tenant ID from header
     * @return paginated list of projects
     */
    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieves a paginated list of projects")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Project>> getAllProjects(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Project> projects = projectService.getAllProjects(tenantId, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Update an existing project.
     * 
     * @param id the project ID
     * @param projectRequest the project update request
     * @param tenantId the tenant ID from header
     * @return the updated project
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Updates an existing project with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully",
                content = @Content(schema = @Schema(implementation = Project.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest projectRequest,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Project updatedProject = projectService.updateProject(id, convertToProject(projectRequest), tenantId);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Change project status.
     * 
     * @param id the project ID
     * @param newStatus the new status
     * @param tenantId the tenant ID from header
     * @return success response
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change project status", description = "Changes the status of an existing project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> changeProjectStatus(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New status", required = true) @RequestBody com.saas.project.entity.ProjectStatus newStatus,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        boolean success = projectService.changeProjectStatus(id, newStatus, tenantId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Project status updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update project status"));
        }
    }

    /**
     * Delete a project.
     * 
     * @param id the project ID
     * @param tenantId the tenant ID from header
     * @return success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Deletes a project (soft delete by archiving)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProject(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID id,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        boolean deleted = projectService.deleteProject(id, tenantId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get projects by status.
     * 
     * @param status the project status
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of projects with specified status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get projects by status", description = "Retrieves projects filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Project>> getProjectsByStatus(
            @Parameter(description = "Project status", required = true) @PathVariable com.saas.project.entity.ProjectStatus status,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Project> projects = projectService.getProjectsByStatus(tenantId, status, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Search projects.
     * 
     * @param searchTerm the search term
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @param tenantId the tenant ID from header
     * @return paginated list of matching projects
     */
    @GetMapping("/search")
    @Operation(summary = "Search projects", description = "Searches projects by name or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Project>> searchProjects(
            @Parameter(description = "Search term", required = true) @RequestParam String searchTerm,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        
        Page<Project> projects = projectService.searchProjects(tenantId, searchTerm, pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get project statistics.
     * 
     * @param tenantId the tenant ID from header
     * @return project statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get project statistics", description = "Retrieves project statistics for the tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('USER') or hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getProjectStatistics(
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        
        Map<String, Object> statistics = projectService.getProjectStatistics(tenantId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Convert ProjectRequest to Project entity.
     * 
     * @param request the DTO to convert
     * @return the Project entity
     */
    private Project convertToProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCode(request.getCode());
        project.setPriority(request.getPriority());
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setEstimatedHours(request.getEstimatedHours());
        project.setBudget(request.getBudget());
        project.setCurrency(request.getCurrency());
        project.setIsPublic(request.getIsPublic());
        project.setTags(request.getTags());
        project.setSettings(request.getSettings());
        return project;
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
