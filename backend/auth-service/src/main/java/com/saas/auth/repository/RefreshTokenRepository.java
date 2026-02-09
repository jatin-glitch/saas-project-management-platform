package com.saas.auth.repository;

import com.saas.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides secure token storage and revocation capabilities.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string.
     * Used for token validation during refresh operations.
     * 
     * @param token the refresh token string
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     * Used for token management and user session tracking.
     * 
     * @param userId the user ID
     * @return List of refresh tokens for the user
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * Find all refresh tokens for a user within a specific tenant.
     * Ensures tenant isolation for token operations.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @return List of refresh tokens for the user in the tenant
     */
    List<RefreshToken> findByUserIdAndTenantId(UUID userId, Long tenantId);

    /**
     * Find valid (non-expired, non-revoked) refresh tokens for a user.
     * Used to determine if user has active sessions.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @return List of valid refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.tenantId = :tenantId " +
           "AND rt.expiresAt > :now AND rt.isRevoked = false")
    List<RefreshToken> findValidTokensByUserAndTenant(
            @Param("userId") UUID userId, 
            @Param("tenantId") Long tenantId, 
            @Param("now") LocalDateTime now);

    /**
     * Find expired refresh tokens.
     * Used for cleanup operations.
     * 
     * @param now the current time
     * @return List of expired refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find revoked refresh tokens older than specified date.
     * Used for cleanup operations.
     * 
     * @param cutoffDate the cutoff date for cleanup
     * @return List of old revoked tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.revokedAt < :cutoffDate")
    List<RefreshToken> findOldRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Revoke all refresh tokens for a user.
     * Used during password change or security incidents.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @param reason the reason for revocation
     * @return number of revoked tokens
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = CURRENT_TIMESTAMP, " +
           "rt.revocationReason = :reason WHERE rt.userId = :userId AND rt.tenantId = :tenantId " +
           "AND rt.isRevoked = false")
    int revokeAllTokensForUser(
            @Param("userId") UUID userId, 
            @Param("tenantId") Long tenantId, 
            @Param("reason") String reason);

    /**
     * Revoke all refresh tokens for a user except specific token.
     * Used during token rotation to maintain single active session.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @param excludeToken the token to exclude from revocation
     * @param reason the reason for revocation
     * @return number of revoked tokens
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = CURRENT_TIMESTAMP, " +
           "rt.revocationReason = :reason WHERE rt.userId = :userId AND rt.tenantId = :tenantId " +
           "AND rt.token != :excludeToken AND rt.isRevoked = false")
    int revokeAllTokensForUserExcept(
            @Param("userId") UUID userId, 
            @Param("tenantId") Long tenantId, 
            @Param("excludeToken") String excludeToken, 
            @Param("reason") String reason);

    /**
     * Count active refresh tokens for a user.
     * Used for session management and security monitoring.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @return number of active refresh tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userId = :userId AND rt.tenantId = :tenantId " +
           "AND rt.expiresAt > :now AND rt.isRevoked = false")
    long countActiveTokensForUser(
            @Param("userId") UUID userId, 
            @Param("tenantId") Long tenantId, 
            @Param("now") LocalDateTime now);

    /**
     * Delete expired refresh tokens.
     * Used for periodic cleanup to maintain database performance.
     * 
     * @param now the current time
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
