package com.saas.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * 
 * Domain events represent significant business occurrences that need to be communicated
 * across different parts of the system or to external services. They are immutable
 * and contain all the context needed for consumers to process the event.
 * 
 * Key design principles:
 * - Events are immutable snapshots of what happened
 * - Events contain tenant context for multi-tenancy
 * - Events have unique IDs for idempotency
 * - Events are serializable for messaging systems
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProjectCreatedEvent.class, name = "PROJECT_CREATED"),
    @JsonSubTypes.Type(value = TaskAssignedEvent.class, name = "TASK_ASSIGNED"),
    @JsonSubTypes.Type(value = TaskStatusChangedEvent.class, name = "TASK_STATUS_CHANGED"),
    @JsonSubTypes.Type(value = IssueCreatedEvent.class, name = "ISSUE_CREATED")
})
public abstract class DomainEvent {
    
    private final String eventId;
    private final Long tenantId;
    private final UUID userId;
    private final String userEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime timestamp;
    
    private final String ipAddress;
    private final String userAgent;
    
    protected DomainEvent(Long tenantId, UUID userId, String userEmail, String ipAddress, String userAgent) {
        this.eventId = UUID.randomUUID().toString();
        this.tenantId = tenantId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.timestamp = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    /**
     * Get the unique identifier for this event instance.
     * Used for idempotency and deduplication.
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * Get the tenant ID this event belongs to.
     * Ensures tenant isolation in event processing.
     */
    public Long getTenantId() {
        return tenantId;
    }
    
    /**
     * Get the user who triggered this event.
     */
    public UUID getUserId() {
        return userId;
    }
    
    /**
     * Get the email of the user who triggered this event.
     */
    public String getUserEmail() {
        return userEmail;
    }
    
    /**
     * Get when this event occurred.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the IP address from which the event was triggered.
     * Used for security auditing.
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * Get the user agent string from the client.
     * Used for debugging and analytics.
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * Get the routing key for this event type.
     * Used by message brokers to route events to appropriate consumers.
     */
    public abstract String getRoutingKey();
    
    /**
     * Get a human-readable description of this event.
     * Used for logging and debugging.
     */
    public abstract String getDescription();
}
