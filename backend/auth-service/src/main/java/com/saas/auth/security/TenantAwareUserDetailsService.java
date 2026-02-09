package com.saas.auth.security;

import com.saas.auth.entity.User;
import com.saas.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tenant-aware UserDetailsService for Spring Security integration.
 * 
 * This service implements Spring Security's UserDetailsService interface
 * with multi-tenant support. It loads user-specific data for authentication
 * while ensuring tenant isolation for security.
 * 
 * Key features:
 * - Tenant-scoped user lookup prevents cross-tenant access
 * - Transactional read-only operations for data consistency
 * - Detailed error messages for debugging (in production, consider generic messages)
 * - Integration with Spring Security's authentication framework
 */
@Service
public class TenantAwareUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param userRepository the user repository for data access
     */
    public TenantAwareUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by username (email) with tenant context.
     * 
     * This method is called by Spring Security during authentication.
     * The tenant ID is extracted from the current security context or
     * request headers to ensure proper tenant isolation.
     * 
     * @param username the email address of the user
     * @return UserDetails implementation containing user information
     * @throws UsernameNotFoundException if user not found or inactive
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Extract tenant ID from current context
        // In a real implementation, this would come from request headers or security context
        Long tenantId = TenantContext.getCurrentTenant();
        
        if (tenantId == null) {
            throw new UsernameNotFoundException("Tenant context not found for user: " + username);
        }

        // Find user by email and tenant ID for security isolation
        User user = userRepository.findByEmailAndTenantId(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User not found with email: %s in tenant: %d", username, tenantId)));

        // Validate user status before allowing authentication
        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    String.format("User account is not active. Status: %s", user.getUserStatus()));
        }

        // Validate email verification
        if (!user.getIsEmailVerified()) {
            throw new UsernameNotFoundException(
                    "User email not verified. Please verify your email address.");
        }

        // Create and return UserPrincipal for Spring Security
        return new UserPrincipal(user);
    }

    /**
     * Load user by email and tenant ID directly.
     * This method is useful for services that have explicit tenant context.
     * 
     * @param email the user's email address
     * @param tenantId the tenant ID
     * @return UserDetails implementation containing user information
     * @throws UsernameNotFoundException if user not found or inactive
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmailAndTenant(String email, Long tenantId) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User not found with email: %s in tenant: %d", email, tenantId)));

        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    String.format("User account is not active. Status: %s", user.getUserStatus()));
        }

        if (!user.getIsEmailVerified()) {
            throw new UsernameNotFoundException(
                    "User email not verified. Please verify your email address.");
        }

        return new UserPrincipal(user);
    }

    /**
     * Check if a user exists by email and tenant ID.
     * Used for registration validation and user management.
     * 
     * @param email the email address to check
     * @param tenantId the tenant ID
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean userExists(String email, Long tenantId) {
        return userRepository.existsByEmailAndTenantId(email, tenantId);
    }
}
