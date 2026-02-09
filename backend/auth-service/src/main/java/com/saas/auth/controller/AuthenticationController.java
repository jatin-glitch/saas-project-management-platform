package com.saas.auth.controller;

import com.saas.auth.dto.AuthResponse;
import com.saas.auth.dto.LoginRequest;
import com.saas.auth.dto.RefreshTokenRequest;
import com.saas.auth.service.AuthenticationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication endpoints.
 * 
 * This controller provides the main authentication API endpoints including:
 * - User login with JWT token generation
 * - Token refresh using refresh tokens
 * - User logout with token revocation
 * - Rate limiting for security protection
 * 
 * Security features:
 * - Rate limiting to prevent brute force attacks
 * - Input validation for all requests
 * - Proper HTTP status codes and error handling
 * - Tenant-aware authentication
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Constructor for dependency injection.
     * 
     * @param authenticationService the authentication service
     */
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate user and generate JWT tokens.
     * 
     * Endpoint: POST /api/auth/login
     * 
     * @param loginRequest the login credentials
     * @param request the HTTP request for tenant extraction
     * @return authentication response with tokens and user info
     */
    @PostMapping("/login")
    @RateLimiter(name = "login", fallbackMethod = "loginFallback")
    public ResponseEntity<AuthResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        // Extract tenant ID from request headers
        Long tenantId = extractTenantId(request);
        
        // Authenticate and generate tokens
        AuthResponse authResponse = authenticationService.authenticate(loginRequest, tenantId);
        
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refresh access token using refresh token.
     * 
     * Endpoint: POST /api/auth/refresh
     * 
     * @param refreshTokenRequest the refresh token request
     * @return new authentication response with fresh tokens
     */
    @PostMapping("/refresh")
    @RateLimiter(name = "refresh", fallbackMethod = "refreshFallback")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        AuthResponse authResponse = authenticationService.refreshToken(refreshTokenRequest);
        
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logout user and revoke tokens.
     * 
     * Endpoint: POST /api/auth/logout
     * 
     * @param request the HTTP request containing the refresh token
     * @return success response
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        // Extract refresh token from request
        String refreshToken = extractRefreshToken(request);
        
        // Revoke refresh token
        authenticationService.logout(refreshToken);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Validate current access token.
     * 
     * Endpoint: GET /api/auth/validate
     * 
     * @return validation response
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken() {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "Token is valid");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for the authentication service.
     * 
     * Endpoint: GET /api/auth/health
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "authentication-service");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }

    /**
     * Extract tenant ID from HTTP request headers.
     * 
     * @param request the HTTP request
     * @return tenant ID, or null if not found
     */
    private Long extractTenantId(HttpServletRequest request) {
        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(tenantHeader.trim());
            } catch (NumberFormatException e) {
                // Log error or handle appropriately
                return null;
            }
        }
        
        // Alternative: extract from subdomain or other header
        String subdomain = extractSubdomain(request);
        if (subdomain != null) {
            // In a real implementation, you would map subdomain to tenant ID
            // This is a placeholder for demonstration
            return null;
        }
        
        return null;
    }

    /**
     * Extract subdomain from request for tenant identification.
     * 
     * @param request the HTTP request
     * @return subdomain, or null if not found
     */
    private String extractSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        if (host != null && host.contains(".")) {
            String[] parts = host.split("\\.");
            if (parts.length > 2) {
                return parts[0]; // First part is the subdomain
            }
        }
        return null;
    }

    /**
     * Extract refresh token from HTTP request.
     * 
     * @param request the HTTP request
     * @return refresh token, or null if not found
     */
    private String extractRefreshToken(HttpServletRequest request) {
        // Try to get from Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try to get from request parameter (less secure, but sometimes needed)
        return request.getParameter("refresh_token");
    }

    // Fallback methods for rate limiting
    public ResponseEntity<Map<String, String>> loginFallback(LoginRequest loginRequest, HttpServletRequest request, Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Too many authentication attempts. Please try again later.");
        response.put("message", "Service temporarily unavailable due to high demand.");
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Map<String, String>> refreshFallback(RefreshTokenRequest refreshTokenRequest, Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Too many refresh attempts. Please try again later.");
        response.put("message", "Service temporarily unavailable due to high demand.");
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
