package com.saas.project.dto;

import com.saas.project.entity.ProjectPriority;
import com.saas.project.entity.ProjectStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Data Transfer Object for Project creation and updates.
 * Contains validation annotations for request body validation.
 */
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Project description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Project code is required")
    @Size(max = 20, message = "Project code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Project code must contain only uppercase letters, numbers, and underscores")
    private String code;

    @NotNull(message = "Project priority is required")
    private ProjectPriority priority;

    private ProjectStatus status;

    @Future(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be after or equal to start date")
    private LocalDate endDate;

    @Min(value = 0, message = "Estimated hours cannot be negative")
    private Integer estimatedHours;

    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    private Double budget;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "USD";

    private Boolean isPublic = false;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Size(max = 2000, message = "Settings must not exceed 2000 characters")
    private String settings;

    // Default constructor
    public ProjectRequest() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    public void setPriority(ProjectPriority priority) {
        this.priority = priority;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "ProjectRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", estimatedHours=" + estimatedHours +
                ", budget=" + budget +
                ", currency='" + currency + '\'' +
                ", isPublic=" + isPublic +
                ", tags='" + tags + '\'' +
                '}';
    }
}
