package com.saas.auth.repository;

import com.saas.auth.entity.User;
import com.saas.auth.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides tenant-aware user data access for authentication and authorization.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email and tenant ID.
     * Used for authentication with tenant isolation.
     * 
     * @param email the user's email
     * @param tenantId the tenant ID
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    /**
     * Find user by email across all tenants.
     * Used for admin operations and user uniqueness validation.
     * 
     * @param email the user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by email and tenant ID.
     * Used for registration validation.
     * 
     * @param email the user's email
     * @param tenantId the tenant ID
     * @return true if user exists, false otherwise
     */
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    /**
     * Find all users within a specific tenant.
     * Used for tenant administration.
     * 
     * @param tenantId the tenant ID
     * @return List of users in the tenant
     */
    List<User> findByTenantId(Long tenantId);

    /**
     * Find users by status within a tenant.
     * Used for user management and reporting.
     * 
     * @param tenantId the tenant ID
     * @param status the user status
     * @return List of users with the specified status
     */
    List<User> findByTenantIdAndUserStatus(Long tenantId, UserStatus status);

    /**
     * Find active users within a tenant.
     * Used for authentication and user management.
     * 
     * @param tenantId the tenant ID
     * @return List of active users
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.userStatus = 'ACTIVE'")
    List<User> findActiveUsersByTenant(@Param("tenantId") Long tenantId);

    /**
     * Count users by tenant.
     * Used for billing and tenant management.
     * 
     * @param tenantId the tenant ID
     * @return number of users in the tenant
     */
    long countByTenantId(Long tenantId);

    /**
     * Count active users by tenant.
     * Used for license management.
     * 
     * @param tenantId the tenant ID
     * @return number of active users in the tenant
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.userStatus = 'ACTIVE'")
    long countActiveUsersByTenant(@Param("tenantId") Long tenantId);

    /**
     * Find users by role within a tenant.
     * Used for role-based access control and administration.
     * 
     * @param tenantId the tenant ID
     * @param role the user role
     * @return List of users with the specified role
     */
    List<User> findByTenantIdAndRole(Long tenantId, com.saas.auth.entity.UserRole role);

    /**
     * Find users who haven't verified their email within a tenant.
     * Used for email verification reminders.
     * 
     * @param tenantId the tenant ID
     * @return List of unverified users
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isEmailVerified = false")
    List<User> findUnverifiedUsersByTenant(@Param("tenantId") Long tenantId);
}
