package com.saas.auth.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * JWT Authentication Filter for processing JWT tokens in HTTP requests.
 * 
 * This filter extends Spring Security's OncePerRequestFilter to ensure
 * it's executed once per request. It validates JWT tokens from the
 * Authorization header and sets up the Spring Security context.
 * 
 * Key features:
 * - Extracts JWT from Authorization header (Bearer token)
 * - Validates token signature and expiration
 * - Sets up tenant context from token claims
 * - Populates Spring Security context with authenticated user
 * - Handles token validation errors gracefully
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for dependency injection.
     * 
     * @param tokenProvider the JWT token provider for validation
     * @param userDetailsService the user details service for loading user data
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filter method to process JWT authentication for each request.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain for continuing request processing
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O operations fail
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Validate token and extract user information
                String userId = tokenProvider.getUserIdFromToken(jwt).toString();
                Long tenantId = tokenProvider.getTenantIdFromToken(jwt);
                
                // Set tenant context for the current request
                TenantContext.setCurrentTenant(tenantId);
                
                // Check if user is already authenticated (optimization)
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user details and create authentication token
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    
                    // Create authentication token with user authorities
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities());
                    
                    // Set authentication details
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException ex) {
            logger.error("JWT token validation failed: " + ex.getMessage());
            // Clear security context on token validation failure
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context: " + ex.getMessage());
            // Clear security context on any authentication error
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     * 
     * @param request the HTTP request
     * @return JWT token string, or null if not found or invalid
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Determine if the filter should be applied to the current request.
     * 
     * This method can be overridden to skip JWT processing for certain endpoints
     * like login, refresh token, or public endpoints.
     * 
     * @param request the HTTP request
     * @return true if filter should be applied, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT processing for authentication endpoints
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/forgot-password") ||
               path.startsWith("/api/auth/reset-password") ||
               path.startsWith("/actuator") ||
               path.startsWith("/health") ||
               path.startsWith("/info");
    }

    /**
     * Cleanup method to clear tenant context after filter processing.
     * This is handled automatically by Spring's filter lifecycle.
     */
    @Override
    public void destroy() {
        // Ensure tenant context is cleared when filter is destroyed
        TenantContext.clear();
        super.destroy();
    }
}
