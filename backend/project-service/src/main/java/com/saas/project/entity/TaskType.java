package com.saas.project.entity;

public enum TaskType {
    FEATURE("Feature"),
    BUG("Bug"),
    IMPROVEMENT("Improvement"),
    TASK("General Task");

    private final String value;

    TaskType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
