package com.saas.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for secure token generation and validation.
 * 
 * This class handles:
 * - Access token generation (short-lived, 15 minutes)
 * - Refresh token generation (long-lived, 7 days)
 * - Token validation and parsing
 * - Multi-tenant token isolation
 * 
 * Security considerations:
 * - Uses HS256 algorithm with strong secret key
 * - Includes tenant ID for multi-tenant isolation
 * - Embeds user roles for authorization
 * - Tokens are stateless but validated against database for refresh tokens
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:900}") // 15 minutes in seconds
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800}") // 7 days in seconds
    private long refreshTokenExpiration;

    @Value("${app.jwt.issuer:saas-auth-service}")
    private String issuer;

    private SecretKey getSigningKey() {
        // Ensure the secret is at least 256 bits (32 bytes) for HS256
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            // Pad with zeros if too short (not recommended for production)
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token for authenticated user.
     * Access tokens are short-lived and contain essential user information.
     * 
     * @param authentication Spring Security authentication object
     * @param tenantId the tenant ID for multi-tenant isolation
     * @return JWT access token
     */
    public String generateAccessToken(Authentication authentication, Long tenantId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000);

        // Extract user roles as strings
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("email", userPrincipal.getEmail())
                .claim("tenantId", tenantId)
                .claim("roles", roles)
                .claim("tokenType", "access")
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate refresh token for token rotation.
     * Refresh tokens are stored in database for revocation support.
     * 
     * @param userId the user ID
     * @param tenantId the tenant ID
     * @return refresh token string
     */
    public String generateRefreshToken(UUID userId, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("tenantId", tenantId)
                .claim("tokenType", "refresh")
                .claim("jti", UUID.randomUUID().toString()) // Unique identifier for token
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT token and parse claims.
     * 
     * @param token the JWT token to validate
     * @return Jws<Claims> containing token claims
     * @throws JwtException if token is invalid or expired
     */
    public Jws<Claims> validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Malformed token", e);
        } catch (SecurityException e) {
            throw new JwtException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid token argument", e);
        }
    }

    /**
     * Extract user ID from JWT token.
     * 
     * @param token the JWT token
     * @return user ID as UUID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract tenant ID from JWT token.
     * 
     * @param token the JWT token
     * @return tenant ID as Long
     */
    public Long getTenantIdFromToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return claims.get("tenantId", Long.class);
    }

    /**
     * Extract email from JWT token.
     * 
     * @param token the JWT token
     * @return email as String
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return claims.get("email", String.class);
    }

    /**
     * Extract roles from JWT token.
     * 
     * @param token the JWT token
     * @return roles as comma-separated String
     */
    public String getRolesFromToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return claims.get("roles", String.class);
    }

    /**
     * Get token expiration date from JWT token.
     * 
     * @param token the JWT token
     * @return expiration date as LocalDateTime
     */
    public LocalDateTime getExpirationFromToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Check if token is an access token.
     * 
     * @param token the JWT token
     * @return true if access token, false otherwise
     */
    public boolean isAccessToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return "access".equals(claims.get("tokenType", String.class));
    }

    /**
     * Check if token is a refresh token.
     * 
     * @param token the JWT token
     * @return true if refresh token, false otherwise
     */
    public boolean isRefreshToken(String token) {
        Claims claims = validateToken(token).getPayload();
        return "refresh".equals(claims.get("tokenType", String.class));
    }

    /**
     * Get remaining validity time for token in seconds.
     * 
     * @param token the JWT token
     * @return remaining seconds, or 0 if expired
     */
    public long getRemainingValidity(String token) {
        try {
            Claims claims = validateToken(token).getPayload();
            Date expiration = claims.getExpiration();
            return (expiration.getTime() - System.currentTimeMillis()) / 1000;
        } catch (JwtException e) {
            return 0;
        }
    }
}
