# Project Service

A production-grade Spring Boot service for Project and Task management in a multi-tenant SaaS application.

## Features

### 🏗️ **Project Management**
- **CRUD Operations** with tenant isolation
- **Project lifecycle management** (planning → active → completed → archived)
- **Team member management** with role-based access
- **Budget and timeline tracking**
- **Project statistics and reporting**

### ✅ **Task Management**
- **Complete task lifecycle** (TODO → IN_PROGRESS → DONE)
- **Task assignment and reassignment**
- **Dependency and subtask management**
- **Priority-based task management**
- **Time tracking and deadline management**
- **Task blocking and unblocking**

### 🔐 **Security & Authorization**
- **Multi-tenant isolation** via X-Tenant-ID header
- **Role-based access control (RBAC)** using existing authentication system
- **Method-level security** with @PreAuthorize annotations
- **Optimistic locking** for concurrent operations
- **Comprehensive validation** with Jakarta Bean Validation

### 📊 **Business Logic**
- **Status transition validation** with business rules
- **Project code uniqueness** within tenants
- **Task dependency cycle detection**
- **Budget tracking** and over-budget alerts
- **Time estimation** vs actual tracking

## Architecture

### Service Layer Design
```
project-service/
├── controller/
│   ├── ProjectController.java    # REST endpoints with OpenAPI
│   └── TaskController.java      # Task management APIs
├── service/
│   ├── ProjectService.java    # Business logic for projects
│   └── TaskService.java      # Business logic for tasks
├── repository/
│   ├── ProjectRepository.java  # Data access with tenant isolation
│   └── TaskRepository.java    # Task data with optimistic locking
└── dto/
    ├── ProjectRequest.java   # Request validation DTOs
    └── TaskRequest.java     # Task request DTOs
```

### Multi-Tenant Security
- **Tenant Isolation**: All operations scoped to tenant ID
- **Header-based**: X-Tenant-ID header for tenant identification
- **Database Queries**: All queries include tenant filtering
- **Authorization Checks**: Users can only access their tenant's data

### Transaction Management
- **@Transactional**: All service methods properly transactional
- **Rollback Support**: Automatic rollback on exceptions
- **Isolation Levels**: READ_ONLY for queries, DEFAULT for writes

## API Endpoints

### Project Management
- `POST /api/projects` - Create project (PROJECT_MANAGER+)
- `GET /api/projects/{id}` - Get project by ID (USER+)
- `GET /api/projects` - List projects with pagination (USER+)
- `PUT /api/projects/{id}` - Update project (PROJECT_MANAGER+)
- `PATCH /api/projects/{id}/status` - Change status (PROJECT_MANAGER+)
- `DELETE /api/projects/{id}` - Archive project (ADMIN+)
- `GET /api/projects/status/{status}` - Filter by status (USER+)
- `GET /api/projects/search` - Search projects (USER+)
- `GET /api/projects/statistics` - Project statistics (USER+)

### Task Management
- `POST /api/tasks` - Create task (USER+)
- `GET /api/tasks/{id}` - Get task by ID (USER+)
- `GET /api/tasks` - List tasks with pagination (USER+)
- `PUT /api/tasks/{id}` - Update task (USER+)
- `PATCH /api/tasks/{id}/status` - Change status (USER+)
- `PATCH /api/tasks/{id}/assign` - Assign task (PROJECT_MANAGER+)
- `GET /api/tasks/project/{projectId}` - Filter by project (USER+)
- `GET /api/tasks/overdue` - Get overdue tasks (USER+)
- `GET /api/tasks/search` - Search tasks (USER+)
- `GET /api/tasks/statistics` - Task statistics (USER+)

## Usage Examples

### Create Project
```bash
curl -X POST http://localhost:8082/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-ID: 1" \
  -d '{
    "name": "New Website Redesign",
    "code": "WEB_REDESIGN_2024",
    "description": "Complete website redesign project",
    "priority": "HIGH",
    "estimatedHours": 120,
    "budget": 15000.00,
    "startDate": "2024-02-01",
    "endDate": "2024-03-31"
  }'
```

### Create Task
```bash
curl -X POST http://localhost:8082/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-ID: 1" \
  -d '{
    "title": "Implement user authentication",
    "taskNumber": "TASK-001",
    "priority": "HIGH",
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "estimatedHours": 40,
    "dueDate": "2024-02-15T10:00:00",
    "storyPoints": 8
  }'
```

### Update Task Status
```bash
curl -X PATCH http://localhost:8082/api/tasks/550e8400-e29b-41d4-a716-446655440001/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-ID: 1" \
  -d 'IN_PROGRESS'
```

### Get Overdue Tasks
```bash
curl -X GET "http://localhost:8082/api/tasks/overdue?page=0&size=10&sort=dueDate&direction=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Tenant-ID: 1"
```

## Business Rules

### Project Status Transitions
```
PLANNING → ACTIVE → ON_HOLD → COMPLETED → ARCHIVED
```

### Task Status Transitions
```
TODO → IN_PROGRESS → IN_REVIEW → TESTING → DONE
```

### Authorization Matrix
```
Endpoint                    | USER | PROJECT_MANAGER | ADMIN | SUPER_ADMIN
---------------------------|-------|----------------|-------|------
Create Project          |   ❌   |       ✅       |  ✅  |    ✅
Update Project          |   ❌   |       ✅       |  ✅  |    ✅
Delete Project          |   ❌   |       ❌       |  ✅  |    ✅
Create Task             |   ✅   |       ✅       |  ✅  |    ✅
Update Own Task         |   ✅   |       ✅       |  ✅  |    ✅
Update Any Task         |   ❌   |       ✅       |  ✅  |    ✅
Assign Task             |   ❌   |       ✅       |  ✅  |    ✅
```

## Production Considerations

### Scalability
- **Database Optimization**: Proper indexing on tenant_id, project_id, status
- **Connection Pooling**: HikariCP for database connections
- **Caching Strategy**: Redis for frequently accessed projects/tasks
- **Pagination**: Efficient for large datasets
- **Async Processing**: For long-running operations

### Security
- **Tenant Isolation**: Database-level security prevents data leaks
- **Input Validation**: Comprehensive validation prevents injection attacks
- **Rate Limiting**: Integration with auth service rate limiting
- **Audit Trail**: All operations logged with user context

### Performance
- **Optimistic Locking**: Prevents lost updates in concurrent scenarios
- **Batch Operations**: Bulk updates reduce database round trips
- **Lazy Loading**: JPA relationships loaded only when needed
- **Query Optimization**: Custom queries for common access patterns

### Monitoring
- **Metrics**: Task completion rates, project lifecycle times
- **Health Checks**: Service availability and database connectivity
- **Logging**: Structured logging with correlation IDs
- **Alerting**: Budget overruns, overdue tasks

## How This Extends for Team Development

### Adding New Features
1. **Project Templates**: Predefined project configurations
2. **Task Templates**: Common task types with default values
3. **Workflow Automation**: Status-based task routing
4. **Notifications**: Email/Slack integrations for deadlines
5. **Reporting**: Advanced analytics and export capabilities

### Integration Points
- **Auth Service**: Already integrated with JWT authentication
- **Common-lib**: Shared utilities and base entities
- **Notification Service**: Ready for task/project notifications
- **File Storage**: Ready for project attachments
- **Search Service**: Elasticsearch integration for advanced search

### Development Workflow
1. **Feature Branch**: Isolated development for new features
2. **Code Review**: Peer review for business logic validation
3. **Integration Testing**: Test with real auth service
4. **Staging Deployment**: Pre-production validation
5. **Production Rollout**: Gradual release with monitoring

This implementation provides a solid foundation for project and task management with enterprise-grade security, scalability, and maintainability. The clean service layer design ensures business logic is properly encapsulated and the repository layer provides efficient data access with tenant isolation.
