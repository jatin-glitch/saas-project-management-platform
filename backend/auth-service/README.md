# Authentication Service

A production-grade Spring Boot authentication service for multi-tenant SaaS applications with JWT-based authentication and role-based access control (RBAC).

## Features

### 🔐 Authentication & Authorization
- **JWT-based stateless authentication** with access and refresh tokens
- **Multi-tenant security** with tenant isolation
- **Role-based access control (RBAC)** with hierarchical roles
- **Password hashing** with BCrypt
- **Method-level security** with Spring Security annotations

### 🛡️ Security Features
- **Token rotation** for refresh tokens
- **Rate limiting** to prevent brute force attacks
- **CORS configuration** for frontend integration
- **Comprehensive exception handling**
- **Tenant-aware authentication context**

### 📊 Monitoring & Management
- **Health checks** and actuator endpoints
- **Metrics export** for Prometheus
- **Structured logging** with correlation IDs
- **Audit trail** support

## Architecture

### Multi-Tenant Design
The service implements **database-level tenant isolation** where each user is scoped to a specific tenant. This ensures:

- **Data isolation**: Users can only access their tenant's data
- **Security boundaries**: Cross-tenant access is prevented by design
- **Scalability**: Easy to add new tenants without code changes

### JWT Flow
```
1. User Login → Validate credentials → Generate JWT tokens
2. Access Token (15 min) → Used for API calls → Auto-refresh
3. Refresh Token (7 days) → Stored in database → Token rotation
4. Token Validation → Verify signature → Check tenant context
```

### Role Hierarchy
```
SUPER_ADMIN
├── Cross-tenant administration
├── System configuration
└── All ADMIN privileges

ADMIN
├── Tenant management
├── User management
└── All PROJECT_MANAGER privileges

PROJECT_MANAGER
├── Project management
├── Team management
└── All USER privileges

USER
└── Basic access to assigned resources
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User authentication
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - User logout
- `GET /api/auth/validate` - Token validation
- `GET /api/auth/health` - Service health check

### Admin Endpoints (Role-based)
- `GET /api/admin/dashboard` - Admin dashboard (ADMIN+)
- `GET /api/admin/users` - User management (ADMIN+)
- `GET /api/admin/tenants` - Tenant management (ADMIN+)

### System Endpoints (Super Admin)
- `GET /api/super-admin/stats` - System statistics (SUPER_ADMIN)
- `GET /api/super-admin/config` - System configuration (SUPER_ADMIN)

## Configuration

### Environment Variables
```bash
# Database
DB_USERNAME=saas_auth_user
DB_PASSWORD=your_secure_password

# JWT Security
JWT_SECRET=your_256_bit_secret_key_here
JWT_ACCESS_TOKEN_EXPIRATION=900  # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION=604800  # 7 days

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourapp.com
```

### Security Configuration
The service uses Spring Security with:
- **Stateless session management** (JWT tokens)
- **BCrypt password encoding**
- **CORS support** for frontend integration
- **Method-level security** annotations

### Rate Limiting
Configured rate limits per endpoint:
- **Login**: 5 attempts per minute
- **Token Refresh**: 10 attempts per minute
- **General Auth**: 20 requests per minute

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    tenant_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    user_status VARCHAR(50) NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    tenant_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revocation_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Usage Examples

### Login Request
```bash
curl -X POST http://localhost:8081/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 1" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "expiresAt": "2024-01-01T12:15:00",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "displayName": "John Doe",
    "role": "USER",
    "tenantId": 1
  }
}
```

### Using Access Token
```bash
curl -X GET http://localhost:8081/auth/api/admin/dashboard \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Tenant-ID: 1"
```

### Refresh Token
```bash
curl -X POST http://localhost:8081/auth/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

## Method-Level Security Examples

### Using @PreAuthorize
```java
@PreAuthorize("hasRole('ADMIN')")
public Map<String, Object> getAdminDashboard() {
    // Only users with ADMIN role can access
}

@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public Map<String, Object> getTenantManagement() {
    // Users with ADMIN or SUPER_ADMIN roles can access
}

@PreAuthorize("#userId == authentication.principal.id")
public Map<String, Object> getUserProfile(String userId) {
    // Users can only access their own data
}
```

### Using Custom Security Expressions
```java
@PreAuthorize("hasRole('ADMIN') and @tenantSecurityService.canAccessTenant(authentication, #tenantId)")
public Map<String, Object> getTenantData(Long tenantId) {
    // Custom tenant access validation
}
```

## Production Considerations

### Security
1. **JWT Secret**: Use a strong, randomly generated 256-bit secret
2. **HTTPS**: Always use HTTPS in production
3. **Database Security**: Use connection pooling and SSL
4. **Environment Variables**: Store secrets in environment variables or secret managers

### Scalability
1. **Horizontal Scaling**: Stateless design enables easy horizontal scaling
2. **Database Optimization**: Use connection pooling and read replicas
3. **Caching**: Consider Redis for token blacklisting
4. **Load Balancing**: Use sticky sessions or JWT-based load balancing

### Monitoring
1. **Metrics**: Export Prometheus metrics for monitoring
2. **Logging**: Structured logging with correlation IDs
3. **Health Checks**: Comprehensive health endpoints
4. **Alerting**: Set up alerts for authentication failures

### Performance
1. **Database Indexing**: Proper indexes on email, tenant_id, and token fields
2. **Connection Pooling**: Optimize database connection pool size
3. **Token Cleanup**: Regular cleanup of expired tokens
4. **Rate Limiting**: Tune rate limits based on traffic patterns

## Development

### Running Locally
```bash
# Start PostgreSQL
docker run --name postgres-auth -e POSTGRES_DB=saas_auth -e POSTGRES_USER=saas_auth_user -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15

# Run the application
./mvnw spring-boot:run
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw test -Dspring.profiles.active=test

# Generate test coverage report
./mvnw jacoco:report
```

## How This Scales in a Real SaaS Product

### Horizontal Scaling
- **Stateless Design**: JWT tokens enable stateless authentication, allowing any instance to handle any request
- **Database Sharding**: Tenant data can be sharded across multiple database instances
- **Microservices Architecture**: Auth service can be independently scaled from other services

### Multi-Tenant Scalability
- **Tenant Isolation**: Database-level isolation ensures security as tenant count grows
- **Resource Allocation**: Different tenant tiers can have different resource allocations
- **Tenant Onboarding**: Automated tenant provisioning with zero-downtime deployment

### Performance Optimization
- **Token Caching**: Frequently validated tokens can be cached in Redis
- **Database Optimization**: Read replicas for authentication queries
- **CDN Integration**: Static assets and API responses cached at edge locations

### Security at Scale
- **Rate Limiting**: Distributed rate limiting across multiple instances
- **Anomaly Detection**: Machine learning for detecting authentication anomalies
- **Zero Trust Architecture**: Every request is authenticated and authorized

### Operational Excellence
- **Observability**: Comprehensive metrics, logging, and tracing
- **Disaster Recovery**: Multi-region deployment with automatic failover
- **Compliance**: Built-in audit trails and compliance reporting

This authentication service provides a solid foundation for a production SaaS application with enterprise-grade security, scalability, and maintainability.
