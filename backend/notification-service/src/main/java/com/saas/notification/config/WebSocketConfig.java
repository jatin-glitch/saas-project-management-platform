package com.saas.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import java.util.Map;

/**
 * WebSocket configuration for real-time notifications.
 * 
 * This configuration sets up:
 * - STOMP endpoints for WebSocket connections
 * - Message broker for routing messages
 * - CORS configuration for cross-origin requests
 * - Security considerations for WebSocket connections
 * 
 * WebSocket endpoints:
 * - /ws-notification - Primary WebSocket endpoint
 * - /ws-notification-secure - Secure WebSocket endpoint (with auth)
 * 
 * Message destinations:
 * - /user/{userId}/notifications - User-specific notifications
 * - /tenant/{tenantId}/notifications - Tenant-wide notifications
 * - /system/notifications - System-wide notifications
 * - /app/* - Application messages
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable simple message broker with these prefixes
        config.enableSimpleBroker("/user", "/tenant", "/system", "/app");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register STOMP endpoints
        registry.addEndpoint("/ws-notification")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        registry.addEndpoint("/ws-notification-secure")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .addInterceptors(new CustomHandshakeInterceptor())
                .withSockJS();
    }
    
    /**
     * Custom handshake handler for WebSocket connections.
     * This would integrate with your authentication system.
     */
    private static class CustomHandshakeHandler implements HandshakeHandler {
        
        @Override
        public boolean doHandshake(@NonNull ServerHttpRequest request,
                                 @NonNull ServerHttpResponse response,
                                 @NonNull WebSocketHandler wsHandler,
                                 @NonNull Map<String, Object> attributes) {
            
            // Extract user information from request
            String userId = extractUserIdFromRequest(request);
            String tenantId = extractTenantIdFromRequest(request);
            
            if (userId != null && tenantId != null) {
                attributes.put("userId", userId);
                attributes.put("tenantId", tenantId);
                return true;
            }
            
            return false;
        }
        
        private String extractUserIdFromRequest(ServerHttpRequest request) {
            // Extract user ID from headers or query parameters
            String userId = request.getHeaders().getFirst("X-User-Id");
            if (userId == null) {
                // Try to extract from query parameters
                String query = request.getURI().getQuery();
                if (query != null && query.contains("userId=")) {
                    userId = query.split("userId=")[1].split("&")[0];
                }
            }
            return userId;
        }
        
        private String extractTenantIdFromRequest(ServerHttpRequest request) {
            // Extract tenant ID from headers or query parameters
            String tenantId = request.getHeaders().getFirst("X-Tenant-Id");
            if (tenantId == null) {
                // Try to extract from query parameters
                String query = request.getURI().getQuery();
                if (query != null && query.contains("tenantId=")) {
                    tenantId = query.split("tenantId=")[1].split("&")[0];
                }
            }
            return tenantId;
        }
    }
    
    /**
     * Custom handshake interceptor for additional validation.
     */
    private static class CustomHandshakeInterceptor implements HandshakeInterceptor {
        
        @Override
        public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                    @NonNull ServerHttpResponse response,
                                    @NonNull WebSocketHandler wsHandler,
                                    @NonNull Map<String, Object> attributes) throws Exception {
            
            // Validate request
            String origin = request.getHeaders().getFirst("Origin");
            if (origin != null && !isOriginAllowed(origin)) {
                response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                return false;
            }
            
            // Add connection metadata
            attributes.put("connectionTime", java.time.LocalDateTime.now());
            attributes.put("userAgent", request.getHeaders().getFirst("User-Agent"));
            attributes.put("remoteAddress", request.getRemoteAddress());
            
            return true;
        }
        
        @Override
        public void afterHandshake(@NonNull ServerHttpRequest request,
                                 @NonNull ServerHttpResponse response,
                                 @NonNull WebSocketHandler wsHandler,
                                 @Nullable Exception exception) {
            
            if (exception != null) {
                System.err.println("WebSocket handshake failed: " + exception.getMessage());
            } else {
                System.out.println("WebSocket handshake completed successfully");
            }
        }
        
        private boolean isOriginAllowed(String origin) {
            // Implement origin validation logic
            // For development, allow all origins
            // For production, validate against allowed origins
            return true;
        }
    }
}
