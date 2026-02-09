package com.saas.auth.security;

import com.saas.auth.entity.User;
import com.saas.auth.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * UserPrincipal implements Spring Security's UserDetails interface.
 * 
 * This class serves as the bridge between our User entity and Spring Security's
 * authentication system, providing user credentials and authorities for security
 * operations while maintaining tenant context.
 * 
 * Key features:
 * - Implements UserDetails for Spring Security integration
 * - Provides role-based authorities for authorization
 * - Maintains tenant context for multi-tenant security
 * - Immutable design for security (credentials can't be changed after creation)
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final Long tenantId;
    private final UserRole role;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor to create UserPrincipal from User entity.
     * 
     * @param user the User entity
     */
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.tenantId = user.getTenantId();
        this.role = user.getRole();
        this.enabled = user.isActive() && user.getIsEmailVerified();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Get user authorities based on their role.
     * In a production system, this could be extended to include permissions
     * from a more complex RBAC system.
     * 
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Get the user's password hash.
     * This is used by Spring Security for authentication.
     * 
     * @return password hash
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Get the username (email in our case).
     * Spring Security uses this as the principal identifier.
     * 
     * @return email address
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Check if the user's account is non-expired.
     * In our system, accounts don't expire based on time.
     * 
     * @return true (always non-expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Check if the user's account is non-locked.
     * In our system, we use user status instead of account locking.
     * 
     * @return true if account is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Could be extended to check user status
    }

    /**
     * Check if the user's credentials are non-expired.
     * Passwords in our system don't expire based on time.
     * 
     * @return true (always non-expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Check if the user is enabled.
     * User must be active and have verified email.
     * 
     * @return true if user is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Getters for additional user information

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public UserRole getRole() {
        return role;
    }

    /**
     * Get the user's full name.
     * 
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user has admin privileges.
     * 
     * @return true if user is admin or super admin
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }

    /**
     * Check if the user has super admin privileges.
     * 
     * @return true if user is super admin
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    /**
     * Check if the user can manage projects.
     * 
     * @return true if user is project manager or higher
     */
    public boolean canManageProjects() {
        return role.ordinal() >= UserRole.PROJECT_MANAGER.ordinal();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", tenantId=" + tenantId +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
