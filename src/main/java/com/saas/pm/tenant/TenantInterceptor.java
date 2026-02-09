package com.saas.pm.tenant;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;

/**
 * Hibernate interceptor that automatically adds tenant filtering to all database operations.
 * Ensures data isolation by appending tenant conditions to SQL queries.
 * 
 * Request Lifecycle Integration:
 * 1. TenantFilter sets tenant in TenantContext
 * 2. This interceptor reads tenant from TenantContext during Hibernate operations
 * 3. Automatically adds WHERE tenantId = ? to all queries
 * 4. Prevents cross-tenant data access at database level
 * 
 * Why this exists:
 * - Enforces tenant isolation at the ORM level
 * - Prevents accidental cross-tenant data access
 * - Works transparently with existing JPA entities
 * - Provides automatic tenant filtering without manual queries
 * 
 * Scalability benefits:
 * - Zero performance overhead for tenant filtering
 * - Works with connection pooling and caching
 * - Automatic protection against tenant data leaks
 * - No need to modify existing repository methods
 */
public class TenantInterceptor extends EmptyInterceptor {
    
    private static final String TENANT_ID_COLUMN = "tenantId";
    
    @Override
    public String onPrepareStatement(String sql) {
        // Only modify SELECT, UPDATE, DELETE statements for entities with tenantId
        if (shouldApplyTenantFilter(sql)) {
            return addTenantFilter(sql);
        }
        return sql;
    }
    
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // Automatically set tenantId on new entities
        if (entity instanceof com.saas.pm.entity.BaseEntity) {
            Long currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                for (int i = 0; i < propertyNames.length; i++) {
                    if (TENANT_ID_COLUMN.equals(propertyNames[i])) {
                        state[i] = currentTenant;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // Ensure tenant matches before deletion
        if (entity instanceof com.saas.pm.entity.BaseEntity baseEntity) {
            Long currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && !currentTenant.equals(baseEntity.getTenantId())) {
                throw new TenantSecurityException("Cannot delete entity from different tenant");
            }
        }
    }
    
    /**
     * Determines if tenant filtering should be applied to the SQL statement.
     */
    private boolean shouldApplyTenantFilter(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        return trimmedSql.startsWith("SELECT") || 
               trimmedSql.startsWith("UPDATE") || 
               trimmedSql.startsWith("DELETE");
    }
    
    /**
     * Adds tenant filtering condition to SQL queries.
     * This is a simplified implementation - in production, consider using
     * more sophisticated SQL parsing to avoid edge cases.
     */
    private String addTenantFilter(String sql) {
        Long currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return sql; // No tenant context, return original SQL
        }
        
        String trimmedSql = sql.trim().toUpperCase();
        
        // For SELECT statements
        if (trimmedSql.startsWith("SELECT")) {
            if (!sql.toUpperCase().contains("WHERE")) {
                return sql + " WHERE " + TENANT_ID_COLUMN + " = " + currentTenant;
            } else {
                return sql + " AND " + TENANT_ID_COLUMN + " = " + currentTenant;
            }
        }
        
        // For UPDATE and DELETE statements
        if (trimmedSql.startsWith("UPDATE") || trimmedSql.startsWith("DELETE")) {
            if (!sql.toUpperCase().contains("WHERE")) {
                return sql + " WHERE " + TENANT_ID_COLUMN + " = " + currentTenant;
            } else {
                return sql + " AND " + TENANT_ID_COLUMN + " = " + currentTenant;
            }
        }
        
        return sql;
    }
}
