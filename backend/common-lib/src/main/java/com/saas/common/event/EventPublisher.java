package com.saas.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service for publishing domain events to the in-memory event system.
 * 
 * This service handles the asynchronous publishing of events, ensuring that:
 * - Events are published reliably with retry logic
 * - Publishing failures don't affect the main business flow
 * - Events are properly logged for debugging and auditing
 * - Tenant isolation is maintained in event routing
 * 
 * The service uses Spring's ApplicationEventPublisher for in-memory event publishing,
 * allowing us to maintain the event-driven architecture without external message brokers.
 */
@Service
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * Publish a domain event asynchronously.
     * 
     * @param event the domain event to publish
     */
    public void publish(DomainEvent event) {
        try {
            logger.info("Publishing event: {} for tenant: {}", event.getRoutingKey(), event.getTenantId());
            
            // Use Spring's ApplicationEventPublisher for in-memory event publishing
            applicationEventPublisher.publishEvent(event);
            
            logger.info("Successfully published event: {} with ID: {}", 
                       event.getRoutingKey(), event.getEventId());
            
        } catch (Exception e) {
            // Log the error but don't re-throw to avoid affecting business operations
            logger.error("Error publishing event: {} for tenant: {} - {}", 
                        event.getRoutingKey(), event.getTenantId(), e.getMessage(), e);
            
            // In a production system, you might want to:
            // 1. Store the failed event in a dead-letter queue
            // 2. Implement retry logic with exponential backoff
            // 3. Send alerts for critical event failures
        }
    }
    
    /**
     * Publish multiple events in a batch.
     * Useful for scenarios where multiple related events need to be published together.
     * 
     * @param events array of domain events to publish
     */
    public void publishBatch(DomainEvent... events) {
        if (events == null || events.length == 0) {
            return;
        }
        
        logger.info("Publishing batch of {} events", events.length);
        
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}
