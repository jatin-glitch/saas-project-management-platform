package com.saas.auth.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiting configuration for authentication endpoints.
 * 
 * This configuration sets up rate limiters to protect against:
 * - Brute force attacks on login endpoints
 * - Token refresh abuse
 * - General API abuse
 * 
 * Rate limits are configured per endpoint and can be adjusted based on:
 * - Security requirements
 * - Expected traffic patterns
 * - Performance considerations
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Configure rate limiter for login endpoints.
     * Limits to 5 attempts per minute per IP address.
     * 
     * @return RateLimiter for login operations
     */
    @Bean
    public RateLimiter loginRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5) // 5 attempts
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(5)) // wait time
                .build();

        return RateLimiter.of("login", config);
    }

    /**
     * Configure rate limiter for token refresh endpoints.
     * Limits to 10 refreshes per minute per token.
     * 
     * @return RateLimiter for token refresh operations
     */
    @Bean
    public RateLimiter refreshTokenRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 refreshes
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(3)) // wait time
                .build();

        return RateLimiter.of("refresh", config);
    }

    /**
     * Configure rate limiter for general authentication operations.
     * Limits to 20 requests per minute per user.
     * 
     * @return RateLimiter for general authentication operations
     */
    @Bean
    public RateLimiter authenticationRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(20) // 20 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(2)) // wait time
                .build();

        return RateLimiter.of("authentication", config);
    }

    /**
     * Configure rate limiter for refresh token operations.
     * Limits to 10 refreshes per minute per token.
     * 
     * @return RateLimiter for refresh token operations
     */
    @Bean
    public RateLimiter refreshTokenLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 refreshes
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(3)) // wait time
                .build();

        return RateLimiter.of("refreshToken", config);
    }

    /**
     * Configure rate limiter for refresh endpoints.
     * Limits to 15 requests per minute per IP address.
     * 
     * @return RateLimiter for refresh operations
     */
    @Bean
    public RateLimiter refreshRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(15) // 15 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(3)) // wait time
                .build();

        return RateLimiter.of("refresh", config);
    }

    /**
     * Create and configure the rate limiter registry.
     * 
     * @return RateLimiterRegistry with all configured limiters
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(20) // 20 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(2)) // wait time
                .build();

        return RateLimiterRegistry.of(defaultConfig);
    }
}
