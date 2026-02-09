package com.saas.pm.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that extracts tenant ID from HTTP headers and sets it in TenantContext.
 * This is the entry point for tenant resolution in the request lifecycle.
 * 
 * Request Lifecycle:
 * 1. HTTP request arrives with X-Tenant-ID header
 * 2. This filter extracts and validates the tenant ID
 * 3. Tenant ID is stored in ThreadLocal TenantContext
 * 4. Request proceeds through application layers
 * 5. TenantInterceptor reads tenant for database operations
 * 6. Finally block clears ThreadLocal to prevent memory leaks
 * 
 * Why this exists:
 * - Centralizes tenant resolution logic
 * - Ensures tenant context is available for entire request
 * - Provides validation and error handling for tenant headers
 * - Guarantees cleanup to prevent memory leaks
 * 
 * Scalability benefits:
 * - Minimal performance overhead (header extraction only)
 * - Thread-safe operation in high-concurrency environments
 * - Automatic cleanup prevents memory leaks
 * - Works with any authentication mechanism
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Execute before other filters
public class TenantFilter implements Filter {
    
    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Extract tenant ID from request header
            String tenantIdHeader = httpRequest.getHeader(TENANT_HEADER_NAME);
            
            if (tenantIdHeader != null && !tenantIdHeader.trim().isEmpty()) {
                try {
                    Long tenantId = Long.parseLong(tenantIdHeader.trim());
                    
                    // Validate tenant ID (basic validation - in production, verify against database)
                    if (tenantId > 0) {
                        TenantContext.setCurrentTenant(tenantId);
                    } else {
                        sendErrorResponse(httpResponse, "Invalid tenant ID format", HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendErrorResponse(httpResponse, "Tenant ID must be a number", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } else {
                // For development/testing, you might want to allow requests without tenant ID
                // In production, you would typically require tenant ID for all requests
                sendErrorResponse(httpResponse, "Missing tenant header: " + TENANT_HEADER_NAME, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Continue with the request
            chain.doFilter(request, response);
            
        } finally {
            // Always clear the tenant context to prevent memory leaks
            // This is crucial for thread pool environments
            TenantContext.clear();
        }
    }
    
    /**
     * Sends a standardized error response for tenant-related issues.
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"error\": \"Tenant Resolution Error\", \"message\": \"%s\", \"status\": %d}", 
                         message, statusCode)
        );
        response.getWriter().flush();
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization - can be used for setup tasks
    }
    
    @Override
    public void destroy() {
        // Filter cleanup - can be used for resource cleanup
    }
}
