package com.saas.project.dto;

import com.saas.project.entity.TaskPriority;
import com.saas.project.entity.TaskStatus;
import com.saas.project.entity.TaskType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Task creation and updates.
 * Contains validation annotations for request body validation.
 */
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    private String description;

    @Size(max = 50, message = "Task number must not exceed 50 characters")
    private String taskNumber;

    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    private TaskType type;

    @Min(value = 0, message = "Estimated hours cannot be negative")
    private Integer estimatedHours;

    @Future(message = "Due date cannot be in the past")
    private LocalDateTime dueDate;

    private LocalDateTime startDate;

    private UUID projectId;

    private UUID assignedToId;

    private UUID parentTaskId;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    private Integer storyPoints;

    // Default constructor
    public TaskRequest() {
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public UUID getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(UUID parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", taskNumber='" + taskNumber + '\'' +
                ", priority=" + priority +
                ", type=" + type +
                ", estimatedHours=" + estimatedHours +
                ", dueDate=" + dueDate +
                ", startDate=" + startDate +
                ", projectId=" + projectId +
                ", assignedToId=" + assignedToId +
                ", parentTaskId=" + parentTaskId +
                ", tags='" + tags + '\'' +
                ", storyPoints=" + storyPoints +
                '}';
    }
}
