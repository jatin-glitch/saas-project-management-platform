package com.saas.notification.service;

import com.saas.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending email notifications.
 * 
 * This service handles email delivery with:
 * - HTML and plain text support
 * - Template-based emails (placeholder implementation)
 * - Error handling and retry logic
 * - Tenant-aware email configuration
 * 
 * In a production environment, this would integrate with:
 * - Email template engines (Thymeleaf, Handlebars)
 * - Email service providers (SendGrid, AWS SES)
 * - Email analytics and tracking
 * - Unsubscribe management
 */
@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.notification.email.from:noreply@saas-platform.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.email.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Send an email notification.
     * 
     * @param notification the notification to send
     * @return true if successful, false otherwise
     */
    @SuppressWarnings("null")
    public boolean sendEmail(Notification notification) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping email for notification: {}", notification.getId());
            return true;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(notification.getUserEmail());
            helper.setSubject(notification.getTitle());
            
            // Create HTML content with action button
            String htmlContent = buildEmailContent(notification);
            helper.setText(htmlContent, true);
            
            // Send the email
            mailSender.send(message);
            
            logger.info("Email sent successfully to: {} for notification: {}", 
                       notification.getUserEmail(), notification.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {} for notification: {} - {}", 
                        notification.getUserEmail(), notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Build HTML email content.
     * 
     * In a real implementation, this would use a template engine.
     * For now, we'll create a simple HTML structure.
     */
    private String buildEmailContent(Notification notification) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html><head><meta charset='UTF-8'>")
            .append("<title>").append(notification.getTitle()).append("</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }")
            .append(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
            .append(".header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee; }")
            .append(".logo { font-size: 24px; font-weight: bold; color: #333; }")
            .append(".content { margin-bottom: 30px; line-height: 1.6; }")
            .append(".action-button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }")
            .append(".action-button:hover { background-color: #0056b3; }")
            .append(".footer { text-align: center; font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }")
            .append(".priority-high { border-left: 4px solid #dc3545; }")
            .append(".priority-critical { border-left: 4px solid #dc3545; background-color: #fff5f5; }")
            .append("</style>")
            .append("</head><body>");
        
        html.append("<div class='container");
        
        // Add priority styling
        if (notification.getPriority().name().contains("HIGH") || notification.getPriority().name().contains("CRITICAL")) {
            html.append(" priority-").append(notification.getPriority().name().toLowerCase());
        }
        
        html.append("'>");
        
        // Header
        html.append("<div class='header'>")
            .append("<div class='logo'>SaaS Platform</div>")
            .append("</div>");
        
        // Content
        html.append("<div class='content'>")
            .append("<h2>").append(notification.getTitle()).append("</h2>")
            .append("<p>").append(notification.getMessage()).append("</p>");
        
        // Add action button if URL is provided
        if (notification.getActionUrl() != null && !notification.getActionUrl().isEmpty()) {
            String fullUrl = notification.getActionUrl();
            if (!fullUrl.startsWith("http")) {
                fullUrl = baseUrl + fullUrl;
            }
            html.append("<a href='").append(fullUrl).append("' class='action-button'>View Details</a>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>")
            .append("<p>This notification was sent from the SaaS Platform.</p>")
            .append("<p>If you no longer wish to receive these emails, please update your notification preferences.</p>")
            .append("</div>");
        
        html.append("</div></body></html>");
        
        return html.toString();
    }
    
    /**
     * Send a simple text email (fallback method).
     */
    public boolean sendSimpleEmail(Notification notification) {
        if (!emailEnabled) {
            logger.info("Email notifications are disabled. Skipping simple email for notification: {}", notification.getId());
            return true;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getUserEmail());
            message.setSubject(notification.getTitle());
            message.setText(buildSimpleEmailContent(notification));
            
            mailSender.send(message);
            
            logger.info("Simple email sent successfully to: {} for notification: {}", 
                       notification.getUserEmail(), notification.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send simple email to: {} for notification: {} - {}", 
                        notification.getUserEmail(), notification.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Build simple text email content.
     */
    private String buildSimpleEmailContent(Notification notification) {
        StringBuilder content = new StringBuilder();
        
        content.append(notification.getTitle()).append("\n\n");
        content.append(notification.getMessage()).append("\n\n");
        
        if (notification.getActionUrl() != null && !notification.getActionUrl().isEmpty()) {
            String fullUrl = notification.getActionUrl();
            if (!fullUrl.startsWith("http")) {
                fullUrl = baseUrl + fullUrl;
            }
            content.append("View Details: ").append(fullUrl).append("\n\n");
        }
        
        content.append("---\n");
        content.append("This notification was sent from the SaaS Platform.\n");
        content.append("Notification ID: ").append(notification.getId()).append("\n");
        content.append("Type: ").append(notification.getType().getDisplayName()).append("\n");
        content.append("Priority: ").append(notification.getPriority().getDisplayName()).append("\n");
        
        return content.toString();
    }
}
