package com.saas.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging.
 * 
 * When applied to a method, this annotation triggers automatic audit logging
 * through the AOP aspect. The audit log captures comprehensive information
 * about the method execution for security, compliance, and monitoring purposes.
 * 
 * Usage examples:
 * 
 * // Basic audit logging
 * @Auditable(action = "CREATE_PROJECT")
 * public Project createProject(Project project) { ... }
 * 
 * // Audit with entity tracking
 * @Auditable(action = "UPDATE_TASK", entityType = "TASK", entityIdParam = "taskId")
 * public Task updateTask(String taskId, TaskUpdate update) { ... }
 * 
 * // Audit with parameter logging
 * @Auditable(action = "DELETE_USER", logParameters = true)
 * public void deleteUser(String userId) { ... }
 * 
 * // Audit with result logging
 * @Auditable(action = "GET_REPORT", logResult = true)
 * public Report generateReport(ReportRequest request) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * The action being performed.
     * If empty, the method name will be used.
     */
    String action() default "";
    
    /**
     * The type of entity being acted upon.
     * Used for entity-level audit tracking and reporting.
     */
    String entityType() default "";
    
    /**
     * The parameter name that contains the entity ID.
     * Used to extract the specific entity ID for audit logging.
     */
    String entityIdParam() default "";
    
    /**
     * Whether to log method parameters.
     * Be careful with sensitive data - parameters are logged as JSON.
     */
    boolean logParameters() default false;
    
    /**
     * Whether to log the method result/return value.
     * Be careful with sensitive data - results are logged as JSON.
     */
    boolean logResult() default false;
    
    /**
     * Whether this is a high-risk operation that requires additional monitoring.
     * High-risk operations may trigger additional security alerts.
     */
    boolean highRisk() default false;
    
    /**
     * Additional description for the audit context.
     * Useful for providing more context about the operation.
     */
    String description() default "";
}
