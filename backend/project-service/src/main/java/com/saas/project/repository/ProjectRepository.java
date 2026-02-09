package com.saas.project.repository;

import com.saas.project.entity.Project;
import com.saas.project.entity.ProjectPriority;
import com.saas.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Project entity operations.
 * 
 * This repository provides tenant-aware project data access with:
 * - Optimistic locking for concurrent operations
 * - Tenant isolation for security
 * - Performance-optimized queries
 * - Support for complex project searches
 * 
 * Key features:
 * - All operations are tenant-scoped for security
 * - Optimistic locking prevents concurrent modification conflicts
 * - Custom queries for common business scenarios
 * - Pagination support for large datasets
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find project by ID and tenant ID.
     * Ensures tenant isolation for security.
     * 
     * @param id project ID
     * @param tenantId tenant ID
     * @return Optional containing the project if found
     */
    Optional<Project> findByIdAndTenantId(UUID id, Long tenantId);

    /**
     * Find project by code and tenant ID.
     * Used for unique project identification within tenant.
     * 
     * @param code project code
     * @param tenantId tenant ID
     * @return Optional containing the project if found
     */
    Optional<Project> findByCodeAndTenantId(String code, Long tenantId);

    /**
     * Find all projects for a specific tenant with pagination.
     * 
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of projects
     */
    Page<Project> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Find projects by status within a tenant.
     * Used for project management dashboards.
     * 
     * @param tenantId tenant ID
     * @param status project status
     * @param pageable pagination information
     * @return Page of projects with specified status
     */
    Page<Project> findByTenantIdAndStatus(Long tenantId, ProjectStatus status, Pageable pageable);

    /**
     * Find projects by priority within a tenant.
     * Used for prioritized project management.
     * 
     * @param tenantId tenant ID
     * @param priority project priority
     * @param pageable pagination information
     * @return Page of projects with specified priority
     */
    Page<Project> findByTenantIdAndPriority(Long tenantId, ProjectPriority priority, Pageable pageable);

    /**
     * Find projects created by a specific user within tenant.
     * Used for user project dashboards.
     * 
     * @param creatorId user ID
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of projects created by the user
     */
    Page<Project> findByCreatorIdAndTenantId(UUID creatorId, Long tenantId, Pageable pageable);

    /**
     * Find active projects for a user within tenant.
     * Used for user dashboards and task assignment.
     * 
     * @param creatorId user ID
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of active projects created by the user
     */
    @Query("SELECT p FROM Project p WHERE p.creatorId = :creatorId AND p.tenantId = :tenantId " +
           "AND p.status IN ('ACTIVE', 'ON_HOLD') ORDER BY p.priority DESC, p.createdAt DESC")
    Page<Project> findActiveProjectsByCreatorAndTenant(
            @Param("creatorId") UUID creatorId, 
            @Param("tenantId") Long tenantId, 
            Pageable pageable);

    /**
     * Find projects that are due within a date range.
     * Used for deadline tracking and notifications.
     * 
     * @param tenantId tenant ID
     * @param startDate start date range
     * @param endDate end date range
     * @param pageable pagination information
     * @return Page of projects due in the specified range
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId " +
           "AND p.endDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.endDate ASC")
    Page<Project> findProjectsDueInDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Search projects by name or description within tenant.
     * Used for project search functionality.
     * 
     * @param tenantId tenant ID
     * @param searchTerm search term
     * @param pageable pagination information
     * @return Page of projects matching the search term
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.priority DESC, p.createdAt DESC")
    Page<Project> searchProjectsByTenant(
            @Param("tenantId") Long tenantId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find projects that are over budget.
     * Used for financial monitoring and alerts.
     * 
     * @param tenantId tenant ID
     * @param pageable pagination information
     * @return Page of projects over budget
     */
    @Query("SELECT p FROM Project p WHERE p.tenantId = :tenantId " +
           "AND p.actualHours > p.estimatedHours " +
           "ORDER BY (p.actualHours - p.estimatedHours) DESC")
    Page<Project> findOverBudgetProjects(
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    /**
     * Count projects by status within tenant.
     * Used for dashboard statistics.
     * 
     * @param tenantId tenant ID
     * @param status project status
     * @return count of projects with specified status
     */
    long countByTenantIdAndStatus(Long tenantId, ProjectStatus status);

    /**
     * Count projects by creator within tenant.
     * Used for user statistics.
     * 
     * @param creatorId user ID
     * @param tenantId tenant ID
     * @return count of projects created by user
     */
    long countByCreatorIdAndTenantId(UUID creatorId, Long tenantId);

    /**
     * Check if project code exists within tenant.
     * Used for validation during project creation.
     * 
     * @param code project code
     * @param tenantId tenant ID
     * @return true if code exists, false otherwise
     */
    boolean existsByCodeAndTenantId(String code, Long tenantId);

    /**
     * Find projects with optimistic locking.
     * Used for concurrent updates to prevent lost updates.
     * 
     * @param id project ID
     * @param tenantId tenant ID
     * @return Optional containing the project with lock
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Project> findByIdAndTenantIdWithLock(
            @Param("id") UUID id, 
            @Param("tenantId") Long tenantId);

    /**
     * Update project status with tenant isolation.
     * Used for bulk status updates.
     * 
     * @param projectId project ID
     * @param tenantId tenant ID
     * @param newStatus new project status
     * @return number of updated projects
     */
    @Modifying
    @Query("UPDATE Project p SET p.status = :newStatus " +
           "WHERE p.id = :projectId AND p.tenantId = :tenantId")
    int updateProjectStatus(
            @Param("projectId") UUID projectId,
            @Param("tenantId") Long tenantId,
            @Param("newStatus") ProjectStatus newStatus);

    /**
     * Archive completed projects older than specified date.
     * Used for maintenance and cleanup operations.
     * 
     * @param tenantId tenant ID
     * @param cutoffDate date before which to archive
     * @return number of archived projects
     */
    @Modifying
    @Query("UPDATE Project p SET p.status = 'ARCHIVED' " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.status = 'COMPLETED' " +
           "AND p.endDate < :cutoffDate")
    int archiveCompletedProjects(
            @Param("tenantId") Long tenantId,
            @Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Get project statistics for tenant.
     * Used for dashboard and reporting.
     * 
     * @param tenantId tenant ID
     * @return project statistics
     */
    @Query("SELECT " +
           "p.status as status, " +
           "COUNT(p) as count, " +
           "SUM(p.estimatedHours) as totalEstimatedHours, " +
           "SUM(p.actualHours) as totalActualHours, " +
           "SUM(p.budget) as totalBudget " +
           "FROM Project p " +
           "WHERE p.tenantId = :tenantId " +
           "GROUP BY p.status")
    List<Object[]> getProjectStatisticsByTenant(@Param("tenantId") Long tenantId);

}
