# API Documentation

This document provides comprehensive API documentation for the Multi-Tenant SaaS Project Management Platform.

## 🌐 Base URL

- **Development**: `http://localhost:8080`
- **Staging**: `https://staging-api.yourdomain.com`
- **Production**: `https://api.yourdomain.com`

## 🔐 Authentication

All API endpoints (except authentication endpoints) require:
- **Authorization Header**: `Bearer <JWT_TOKEN>`
- **Tenant Header**: `X-Tenant-ID: <TENANT_ID>`

### Authentication Flow

1. **Login** - Obtain JWT tokens
2. **Include Token** - Add to Authorization header
3. **Refresh Token** - Use refresh token when access expires
4. **Logout** - Revoke tokens

## 📚 API Endpoints

### Authentication Service

#### POST /api/auth/login
Authenticate user and receive JWT tokens.

**Headers:**
- `X-Tenant-ID: string` (required) - Tenant identifier

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PROJECT_MANAGER",
    "tenantId": 1
  }
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Missing tenant header or invalid input
- `429 Too Many Requests` - Rate limit exceeded

---

#### POST /api/auth/refresh
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "PROJECT_MANAGER",
    "tenantId": 1
  }
}
```

---

#### POST /api/auth/logout
Logout user and revoke tokens.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Response (200 OK):**
```json
{
  "message": "Logout successful"
}
```

---

#### GET /api/auth/validate
Validate current access token.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Response (200 OK):**
```json
{
  "valid": true,
  "message": "Token is valid",
  "timestamp": 1640995200000
}
```

---

#### GET /api/auth/health
Health check for authentication service.

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "authentication-service",
  "timestamp": "1640995200000"
}
```

### Project Management

#### GET /api/projects
Retrieve paginated list of projects.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Query Parameters:**
- `page: number` (default: 0) - Page number
- `size: number` (default: 20) - Page size
- `sort: string` (default: "createdAt") - Sort field
- `direction: string` (default: "desc") - Sort direction ("asc" or "desc")

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Project Alpha",
      "description": "Description of project",
      "code": "PROJ-001",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "startDate": "2024-01-01",
      "endDate": "2024-12-31",
      "estimatedHours": 1000,
      "budget": 50000,
      "currency": "USD",
      "isPublic": false,
      "tags": ["web", "frontend"],
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z",
      "tenantId": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

---

#### POST /api/projects
Create a new project.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Request Body:**
```json
{
  "name": "New Project",
  "description": "Project description",
  "code": "PROJ-002",
  "status": "PLANNING",
  "priority": "MEDIUM",
  "startDate": "2024-02-01",
  "endDate": "2024-12-31",
  "estimatedHours": 800,
  "budget": 40000,
  "currency": "USD",
  "isPublic": false,
  "tags": ["backend", "api"]
}
```

**Response (201 Created):**
```json
{
  "id": "new-uuid",
  "name": "New Project",
  "description": "Project description",
  "code": "PROJ-002",
  "status": "PLANNING",
  "priority": "MEDIUM",
  "startDate": "2024-02-01",
  "endDate": "2024-12-31",
  "estimatedHours": 800,
  "budget": 40000,
  "currency": "USD",
  "isPublic": false,
  "tags": ["backend", "api"],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "tenantId": 1
}
```

**Error Responses:**
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Insufficient permissions
- `429 Too Many Requests` - Rate limit exceeded

---

#### GET /api/projects/{id}
Retrieve a specific project by ID.

**Path Parameters:**
- `id: string` (required) - Project UUID

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "Project Alpha",
  "description": "Description of project",
  "code": "PROJ-001",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "estimatedHours": 1000,
  "budget": 50000,
  "currency": "USD",
  "isPublic": false,
  "tags": ["web", "frontend"],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "tenantId": 1
}
```

**Error Responses:**
- `404 Not Found` - Project not found
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Access to project denied

---

#### PUT /api/projects/{id}
Update an existing project.

**Path Parameters:**
- `id: string` (required) - Project UUID

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Request Body:** Same as POST /api/projects

**Response (200 OK):** Updated project object

---

#### DELETE /api/projects/{id}
Delete a project (soft delete by archiving).

**Path Parameters:**
- `id: string` (required) - Project UUID

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Response (200 OK):**
```json
{
  "message": "Project deleted successfully"
}
```

---

#### GET /api/projects/search
Search projects by name or description.

**Query Parameters:**
- `searchTerm: string` (required) - Search term
- `page: number` (default: 0) - Page number
- `size: number` (default: 20) - Page size
- `sort: string` (default: "createdAt") - Sort field
- `direction: string` (default: "desc") - Sort direction

**Response (200 OK):** Paginated list of matching projects

---

#### GET /api/projects/statistics
Get project statistics for the tenant.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Response (200 OK):**
```json
{
  "totalProjects": 25,
  "activeProjects": 15,
  "completedProjects": 8,
  "onHoldProjects": 2,
  "projectsByStatus": {
    "PLANNING": 5,
    "IN_PROGRESS": 15,
    "ON_HOLD": 2,
    "COMPLETED": 8,
    "CANCELLED": 0
  },
  "projectsByPriority": {
    "LOW": 8,
    "MEDIUM": 10,
    "HIGH": 5,
    "CRITICAL": 2
  }
}
```

### Task Management

#### GET /api/tasks
Retrieve paginated list of tasks.

**Headers:**
- `Authorization: Bearer <TOKEN>` (required)
- `X-Tenant-ID: string` (required)

**Query Parameters:**
- `page: number` (default: 0) - Page number
- `size: number` (default: 20) - Page size
- `sort: string` (default: "createdAt") - Sort field
- `direction: string` (default: "desc") - Sort direction

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Implement user authentication",
      "description": "Create login and registration functionality",
      "taskNumber": "TASK-001",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "type": "FEATURE",
      "projectId": "project-uuid",
      "assignedToId": "user-uuid",
      "dueDate": "2024-02-15",
      "startDate": "2024-02-01",
      "estimatedHours": 40,
      "storyPoints": 8,
      "tags": ["authentication", "security"],
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z",
      "tenantId": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

---

#### POST /api/tasks
Create a new task.

**Request Body:**
```json
{
  "title": "New Task",
  "description": "Task description",
  "taskNumber": "TASK-002",
  "status": "TODO",
  "priority": "MEDIUM",
  "type": "FEATURE",
  "projectId": "project-uuid",
  "assignedToId": "user-uuid",
  "dueDate": "2024-03-01",
  "startDate": "2024-02-15",
  "estimatedHours": 20,
  "storyPoints": 5,
  "tags": ["frontend", "ui"]
}
```

**Response (201 Created):** Created task object

---

#### GET /api/tasks/project/{projectId}
Get tasks for a specific project.

**Path Parameters:**
- `projectId: string` (required) - Project UUID

**Response (200 OK):** Paginated list of tasks for the project

---

#### PATCH /api/tasks/{id}/status
Change task status.

**Path Parameters:**
- `id: string` (required) - Task UUID

**Request Body:**
```json
"IN_PROGRESS"
```

**Response (200 OK):**
```json
{
  "message": "Task status updated successfully"
}
```

---

#### PATCH /api/tasks/{id}/assign
Assign task to a user.

**Path Parameters:**
- `id: string` (required) - Task UUID

**Request Body:**
```json
"user-uuid"
```

**Response (200 OK):**
```json
{
  "message": "Task assigned successfully"
}
```

---

#### GET /api/tasks/statistics
Get task statistics for the tenant.

**Response (200 OK):**
```json
{
  "totalTasks": 150,
  "todoTasks": 45,
  "inProgressTasks": 60,
  "doneTasks": 45,
  "overdueTasks": 12,
  "tasksByStatus": {
    "TODO": 45,
    "IN_PROGRESS": 60,
    "DONE": 45
  },
  "tasksByPriority": {
    "LOW": 30,
    "MEDIUM": 60,
    "HIGH": 45,
    "CRITICAL": 15
  },
  "tasksByType": {
    "FEATURE": 90,
    "BUG": 30,
    "IMPROVEMENT": 20,
    "DOCUMENTATION": 10
  }
}
```

## 🔧 Error Handling

### Standard Error Response Format
```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": 1640995200000,
  "path": "/api/projects"
}
```

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required or invalid
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict
- `422 Unprocessable Entity` - Validation errors
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

## 📊 Rate Limiting

### Rate Limits
- **Authentication endpoints**: 5 requests per minute
- **API endpoints**: 100 requests per minute per user
- **Search endpoints**: 20 requests per minute per user

### Rate Limit Headers
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995800
```

## 🔍 WebSocket API

### Connection
```javascript
const ws = new WebSocket('ws://localhost:8080/ws?token=<JWT_TOKEN>&tenantId=<TENANT_ID>');
```

### Message Format
```json
{
  "type": "NOTIFICATION|PROJECT_UPDATE|TASK_UPDATE",
  "data": {
    // Event-specific data
  },
  "timestamp": 1640995200000
}
```

### Notification Types
- **NOTIFICATION**: System notifications
- **PROJECT_UPDATE**: Project changes
- **TASK_UPDATE**: Task changes

## 🧪 Testing

### API Testing with curl

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 1" \
  -d '{"email":"user@example.com","password":"password123"}'

# Create Project
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "X-Tenant-ID: 1" \
  -d '{"name":"Test Project","description":"Test","code":"TEST-001","status":"PLANNING","priority":"MEDIUM","isPublic":false,"tags":[]}'
```

### Postman Collection
Import the Postman collection from `docs/postman-collection.json` for easy API testing.

## 📖 Interactive Documentation

### Swagger UI
Access interactive API documentation at:
- **Development**: http://localhost:8080/swagger-ui.html
- **Staging**: https://staging-api.yourdomain.com/swagger-ui.html
- **Production**: https://api.yourdomain.com/swagger-ui.html

### OpenAPI Specification
Download the OpenAPI specification:
- **JSON**: http://localhost:8080/v3/api-docs
- **YAML**: http://localhost:8080/v3/api-docs.yaml

---

For additional API support, refer to the main [README.md](../README.md) or create an issue.
