package com.saas.pm.tenant;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Automatically resolves current tenant and user for JPA auditing.
 * Integrates with Spring Data JPA to set createdBy/updatedBy fields.
 * 
 * Why this exists:
 * - Automatic tenant ID injection for new entities
 * - Seamless integration with Spring Data auditing
 * - Reduces boilerplate in service layers
 * - Ensures audit trail consistency
 */
@Component
public class TenantIdentifierResolver implements AuditorAware<Long> {
    
    @Override
    public Optional<Long> getCurrentAuditor() {
        // In a real implementation, this would return the current user ID
        // For now, we'll return a placeholder or extract from security context
        return Optional.ofNullable(TenantContext.getCurrentTenant());
    }
}
