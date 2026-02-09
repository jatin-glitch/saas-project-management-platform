package com.saas.pm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ProjectMember entity represents the many-to-many relationship between projects and users.
 * Defines user roles within projects and access permissions.
 * Enables granular control over who can access and modify project resources.
 */
@Entity
@Table(name = "project_members")
public class ProjectMember extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ProjectRole role = ProjectRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "can_view_reports", nullable = false)
    private Boolean canViewReports = false;

    @Column(name = "can_manage_tasks", nullable = false)
    private Boolean canManageTasks = false;

    @Column(name = "can_manage_members", nullable = false)
    private Boolean canManageMembers = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public ProjectMember() {
        super();
    }

    public ProjectMember(Project project, User user, ProjectRole role, Long tenantId) {
        super(tenantId);
        this.project = project;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Boolean getCanViewReports() {
        return canViewReports;
    }

    public void setCanViewReports(Boolean canViewReports) {
        this.canViewReports = canViewReports;
    }

    public Boolean getCanManageTasks() {
        return canManageTasks;
    }

    public void setCanManageTasks(Boolean canManageTasks) {
        this.canManageTasks = canManageTasks;
    }

    public Boolean getCanManageMembers() {
        return canManageMembers;
    }

    public void setCanManageMembers(Boolean canManageMembers) {
        this.canManageMembers = canManageMembers;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

/**
 * Project role enumeration
 */
enum ProjectRole {
    OWNER,
    ADMIN,
    PROJECT_MANAGER,
    DEVELOPER,
    DESIGNER,
    TESTER,
    MEMBER,
    VIEWER
}
