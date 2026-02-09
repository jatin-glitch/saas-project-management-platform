package com.saas.common.dto;

/**
 * Generic API response wrapper for consistent response format across the platform.
 * Provides type-safe response building and validation.
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a success response with validation.
     * 
     * @param data the response data
     * @param <T> the data type
     * @return success response
     * @throws IllegalArgumentException if data is null when required
     */
    public static <T> ApiResponse<T> success(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Response data cannot be null for success response");
        }
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Creates a success response with custom message and validation.
     * 
     * @param message the success message
     * @param data the response data
     * @param <T> the data type
     * @return success response
     * @throws IllegalArgumentException if message or data is null
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Success message cannot be null or empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("Response data cannot be null for success response");
        }
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with validation.
     * 
     * @param message the error message
     * @param <T> the data type
     * @return error response
     * @throws IllegalArgumentException if message is null or empty
     */
    public static <T> ApiResponse<T> error(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Creates an error response with data (for validation errors).
     * 
     * @param message the error message
     * @param data the error details
     * @param <T> the data type
     * @return error response with data
     * @throws IllegalArgumentException if message is null or empty
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        return new ApiResponse<>(false, message, data);
    }

    /**
     * Builder pattern for creating API responses.
     */
    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;

        public Builder<T> success() {
            this.success = true;
            return this;
        }

        public Builder<T> error() {
            this.success = false;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be null or empty");
            }
            return new ApiResponse<>(success, message, data);
        }
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
