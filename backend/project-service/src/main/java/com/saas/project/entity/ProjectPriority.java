package com.saas.project.entity;

public enum ProjectPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String value;

    ProjectPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
