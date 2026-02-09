# Event-Driven Notification System & Activity Logging

## Overview

This document describes the event-driven notification system and activity logging implementation for the multi-tenant SaaS platform. The system provides real-time notifications, comprehensive audit trails, and reliable message processing with tenant isolation.

## Architecture

### Core Components

1. **Domain Events** - Immutable events representing business actions
2. **Event Publisher** - Asynchronous event publishing with Spring Cloud Stream
3. **Notification Service** - Multi-channel notification processing
4. **Audit System** - AOP-based activity logging
5. **WebSocket Service** - Real-time notification delivery
6. **Email Service** - Email notification handling
7. **Reliability Features** - Retry logic, cleanup, and monitoring

### Event Flow

```
Business Service → Domain Event → Message Broker → Event Consumer → Notification → Multiple Channels
```

1. Business services publish domain events
2. RabbitMQ routes events to appropriate consumers
3. Notification service processes events and creates notifications
4. Notifications are delivered via configured channels (in-app, email, WebSocket)

## Domain Events

### Event Types

- **ProjectCreatedEvent** - Project creation notifications
- **TaskAssignedEvent** - Task assignment notifications  
- **TaskStatusChangedEvent** - Task status change notifications
- **IssueCreatedEvent** - Issue creation notifications

### Event Structure

All events extend the base `DomainEvent` class and include:
- Event ID (for idempotency)
- Tenant ID (for multi-tenancy)
- User information (who triggered the event)
- Timestamp and execution context
- Event-specific data

## Notification System

### Multi-Channel Delivery

- **In-App Notifications** - Database-backed notification center
- **Email Notifications** - HTML email delivery with templates
- **WebSocket Notifications** - Real-time push notifications
- **Future Channels** - SMS, push notifications, webhooks

### Notification Features

- Tenant isolation for security
- Priority-based processing (CRITICAL, HIGH, MEDIUM, LOW)
- Idempotent processing to prevent duplicates
- Retry logic with exponential backoff
- Expiration and cleanup
- Read/unread status tracking

### Notification Types

- Project notifications (created, updated, deleted)
- Task notifications (assigned, status changed, completed)
- Issue notifications (created, assigned, resolved)
- System notifications (maintenance, announcements)
- User notifications (welcome, profile updates)

## Audit Trail System

### AOP-Based Logging

The `@Auditable` annotation enables automatic audit logging:

```java
@Auditable(action = "CREATE_PROJECT", entityType = "PROJECT", logParameters = true)
public Project createProject(Project project) {
    // Business logic
}
```

### Audit Information Captured

- **Who** - User ID, email, IP address, user agent
- **What** - Action performed, entity type and ID, parameters
- **When** - Timestamp, execution time
- **Where** - IP address, session ID, request ID
- **How** - Success/failure status, error messages

### Audit Features

- Asynchronous logging to avoid blocking operations
- Tenant-isolated audit trails
- Comprehensive search and filtering
- Data retention policies
- Compliance and security support

## Reliability & Performance

### Idempotency

- Event ID-based duplicate prevention
- Database constraints for unique events
- Idempotent message consumption

### Retry Logic

- Configurable retry attempts and delays
- Exponential backoff for failed notifications
- Dead-letter queue handling
- Circuit breaker patterns

### Monitoring & Health

- Scheduled health checks every 10 minutes
- Failure rate monitoring and alerting
- Performance metrics collection
- Daily and weekly maintenance tasks

### Cleanup & Retention

- Automatic cleanup of expired notifications
- Old read notification removal (configurable retention)
- Audit log retention policies
- Database optimization

## Configuration

### RabbitMQ Configuration

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  cloud:
    stream:
      bindings:
        project-created:
          destination: project.created
          group: notification-service
```

### Notification Configuration

```yaml
app:
  notification:
    email:
      enabled: true
      from: noreply@saas-platform.com
    websocket:
      enabled: true
      heartbeat-interval: 30000
    cleanup:
      old-notifications-days: 90
      expired-notifications-hours: 1
    retry:
      max-attempts: 3
      delay-ms: 5000
```

## WebSocket Endpoints

### Connection Endpoints

- `/ws-notification` - Standard WebSocket endpoint
- `/ws-notification-secure` - Secure endpoint with authentication

### Message Destinations

- `/user/{userId}/notifications` - User-specific notifications
- `/tenant/{tenantId}/notifications` - Tenant-wide notifications
- `/system/notifications` - System-wide notifications

### Message Format

```json
{
  "id": "notification-uuid",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "You have been assigned a new task",
  "priority": "HIGH",
  "entityType": "TASK",
  "entityId": "task-uuid",
  "actionUrl": "/tasks/task-uuid",
  "timestamp": "2024-01-01T10:00:00",
  "isUrgent": true,
  "timeAgo": "2 minutes ago"
}
```

## Testing

### Integration Tests

The system includes comprehensive integration tests covering:
- Event publishing and consumption
- Notification creation and delivery
- Multi-channel processing
- Idempotency and reliability
- Audit trail generation

### Test Scenarios

1. Project creation triggers appropriate notifications
2. Task assignment creates real-time notifications
3. Task status changes generate correct notifications
4. Issue creation creates high-priority notifications
5. System handles failures gracefully

## Security Considerations

### Tenant Isolation

- All data access is tenant-scoped
- Event routing includes tenant context
- WebSocket connections validated for tenant access
- Audit logs separated by tenant

### Data Protection

- Sensitive data filtering in audit logs
- Configurable parameter logging
- Secure WebSocket connections
- Email template sanitization

### Authentication & Authorization

- JWT-based authentication for WebSocket
- Role-based access control
- Request validation and rate limiting
- IP-based security monitoring

## Scalability & Evolution

### Horizontal Scaling

- Stateless notification service design
- Message broker clustering support
- Database sharding by tenant
- Load balancer-friendly WebSocket endpoints

### Performance Optimization

- Asynchronous processing throughout
- Database query optimization
- Connection pooling and caching
- Batch processing for bulk operations

### Future Enhancements

1. **Advanced Analytics** - Notification engagement metrics
2. **Machine Learning** - Intelligent notification routing
3. **Multi-Language Support** - Internationalized notifications
4. **Advanced Templates** - Dynamic email templates
5. **Mobile Push** - Native mobile app notifications
6. **Webhook Integration** - External system notifications
7. **Workflow Automation** - Custom notification rules

## Production Deployment

### Environment Configuration

- Development: H2 database, local RabbitMQ
- Production: PostgreSQL, clustered RabbitMQ
- Email: SMTP integration with templates
- Monitoring: Prometheus metrics, health endpoints

### Monitoring & Observability

- Spring Boot Actuator endpoints
- Custom health indicators
- Performance metrics collection
- Error tracking and alerting
- Audit log analysis

### Backup & Recovery

- Database backup strategies
- Message broker durability
- Audit log archiving
- Disaster recovery procedures

## Best Practices

### Event Design

- Keep events immutable and serializable
- Include all necessary context
- Use meaningful event names
- Version events for compatibility

### Notification Design

- Respect user preferences
- Implement rate limiting
- Provide clear action buttons
- Use appropriate priority levels

### Audit Design

- Log before and after states
- Include execution context
- Use structured logging
- Implement retention policies

### Error Handling

- Graceful degradation
- Comprehensive logging
- User-friendly error messages
- Automatic recovery mechanisms

This event-driven system provides a robust, scalable foundation for real-time notifications and comprehensive audit trails in a multi-tenant SaaS environment.
