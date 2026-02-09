package com.saas.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Notification Service.
 * 
 * This service is responsible for:
 * - Processing domain events from other services
 * - Managing notifications across multiple channels
 * - Providing real-time WebSocket notifications
 * - Handling email notifications
 * - Maintaining audit trails
 * 
 * Key features enabled:
 * - @EnableAsync: For asynchronous notification processing
 * - @EnableScheduling: For cleanup and retry jobs
 * 
 * Architecture:
 * - Event-driven with RabbitMQ/Spring Cloud Stream
 * - Multi-tenant isolation
 * - Asynchronous processing
 * - Real-time delivery via WebSocket
 * - Persistent storage with audit trail
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
