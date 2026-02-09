package com.saas.pm.tenant.config;

import com.saas.pm.tenant.TenantInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Spring Boot configuration for multi-tenant database setup.
 * Configures Hibernate to use tenant-aware interceptor and sets up JPA with tenant isolation.
 * 
 * Integration Flow:
 * 1. DataSource configured for database connections
 * 2. EntityManagerFactory created with tenant interceptor
 * 3. TransactionManager set up for multi-tenant operations
 * 4. TenantInterceptor automatically applied to all JPA operations
 * 
 * Why this exists:
 * - Centralizes multi-tenant database configuration
 * - Ensures Hibernate uses tenant-aware interceptor
 * - Provides proper transaction management for tenant isolation
 * - Configures JPA properties for multi-tenancy
 * 
 * Scalability benefits:
 * - Works with connection pooling for high performance
 * - Automatic tenant filtering reduces database load
 * - Transaction isolation prevents cross-tenant contamination
 * - Configurable for different isolation strategies
 */
@Configuration
public class MultiTenantConfiguration {
    
    /**
     * Configures the EntityManagerFactory with tenant-aware Hibernate interceptor.
     * This ensures all JPA operations are tenant-isolated.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, TenantInterceptor tenantInterceptor) {
        
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.saas.pm.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        
        // Configure Hibernate properties for multi-tenancy
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // Use validate in production
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "false");
        jpaProperties.put("hibernate.use_sql_comments", "false");
        
        // Enable second-level cache for better performance
        jpaProperties.put("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        
        // Configure the tenant interceptor
        jpaProperties.put("hibernate.session_factory.interceptor", tenantInterceptor);
        
        // Enable batch processing for better performance
        jpaProperties.put("hibernate.jdbc.batch_size", "20");
        jpaProperties.put("hibernate.order_inserts", "true");
        jpaProperties.put("hibernate.order_updates", "true");
        
        // Configure connection pool settings
        jpaProperties.put("hibernate.connection.provider_disables_autocommit", "true");
        jpaProperties.put("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT");
        
        emf.setJpaProperties(jpaProperties);
        
        return emf;
    }
    
    /**
     * Creates the tenant interceptor bean.
     * This interceptor will be automatically applied to all Hibernate sessions.
     */
    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor();
    }
    
    /**
     * Configures the transaction manager for multi-tenant operations.
     * Ensures proper transaction boundaries and rollback behavior.
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Configure transaction properties for multi-tenancy
        Properties transactionProperties = new Properties();
        transactionProperties.put("hibernate.transaction.jta.platform", "org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform");
        
        return transactionManager;
    }
    
    /**
     * Optional: Configure tenant-specific data source routing.
     * Uncomment and implement for schema-per-tenant strategy.
     */
    /*
    @Bean
    @Primary
    public DataSource routingDataSource() {
        MultiTenantRoutingDataSource routingDataSource = new MultiTenantRoutingDataSource();
        
        Map<Object, Object> dataSources = new HashMap<>();
        // Configure tenant-specific data sources here
        // dataSources.put("tenant1", tenant1DataSource());
        // dataSources.put("tenant2", tenant2DataSource());
        
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource());
        
        return routingDataSource;
    }
    */
}
