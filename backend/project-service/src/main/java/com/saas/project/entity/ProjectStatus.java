package com.saas.project.entity;

public enum ProjectStatus {
    PLANNING("Planning"),
    ACTIVE("Active"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    ARCHIVED("Archived");

    private final String value;

    ProjectStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
