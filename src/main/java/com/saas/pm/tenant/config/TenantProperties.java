package com.saas.pm.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for multi-tenant settings.
 * Allows customization of tenant resolution and isolation behavior.
 * 
 * Why this exists:
 * - Externalizes tenant configuration for different environments
 * - Enables runtime configuration without code changes
 * - Supports different tenant isolation strategies
 * - Provides flexibility for tenant validation rules
 */
@Component
@ConfigurationProperties(prefix = "app.tenant")
public class TenantProperties {
    
    /**
     * Header name for tenant ID extraction from HTTP requests.
     */
    private String headerName = "X-Tenant-ID";
    
    /**
     * Whether to require tenant ID in all requests.
     */
    private boolean requireTenantId = true;
    
    /**
     * Default tenant ID for development/testing.
     */
    private Long defaultTenantId = null;
    
    /**
     * Whether to enable tenant validation against database.
     */
    private boolean enableTenantValidation = true;
    
    /**
     * Maximum number of tenants allowed (for licensing).
     */
    private Integer maxTenants = null;
    
    /**
     * Whether to log tenant resolution for debugging.
     */
    private boolean enableTenantLogging = false;
    
    // Getters and Setters
    public String getHeaderName() {
        return headerName;
    }
    
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
    
    public boolean isRequireTenantId() {
        return requireTenantId;
    }
    
    public void setRequireTenantId(boolean requireTenantId) {
        this.requireTenantId = requireTenantId;
    }
    
    public Long getDefaultTenantId() {
        return defaultTenantId;
    }
    
    public void setDefaultTenantId(Long defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }
    
    public boolean isEnableTenantValidation() {
        return enableTenantValidation;
    }
    
    public void setEnableTenantValidation(boolean enableTenantValidation) {
        this.enableTenantValidation = enableTenantValidation;
    }
    
    public Integer getMaxTenants() {
        return maxTenants;
    }
    
    public void setMaxTenants(Integer maxTenants) {
        this.maxTenants = maxTenants;
    }
    
    public boolean isEnableTenantLogging() {
        return enableTenantLogging;
    }
    
    public void setEnableTenantLogging(boolean enableTenantLogging) {
        this.enableTenantLogging = enableTenantLogging;
    }
}
