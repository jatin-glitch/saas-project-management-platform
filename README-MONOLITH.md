# SaaS Platform - Monolith Version

A simplified version of the SaaS platform running as a single Spring Boot application without Kafka, Zookeeper, or microservices complexity.

## 🚀 Quick Start

### Using Docker Compose (Recommended)

1. **Start the monolith application**
   ```bash
   docker compose -f docker-compose.monolith.yml up -d
   ```

2. **Access the application**
   - Frontend: http://localhost:80
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Local Development

1. **Start infrastructure services**
   ```bash
   docker compose -f docker-compose.monolith.yml up postgres redis -d
   ```

2. **Build and run the monolith**
   ```bash
   cd backend
   mvn clean install
   cd monolith
   mvn spring-boot:run
   ```

3. **Start frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## 📋 Available Services

All services are now combined into a single application:

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/logout` - User logout
- `GET /api/auth/validate` - Token validation
- `GET /api/auth/health` - Health check

### Project Endpoints
- `GET /api/projects` - List projects (paginated)
- `POST /api/projects` - Create project
- `GET /api/projects/{id}` - Get project by ID
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project
- `GET /api/projects/statistics` - Project statistics

### Task Endpoints
- `GET /api/tasks` - List tasks (paginated)
- `POST /api/tasks` - Create task
- `GET /api/tasks/{id}` - Get task by ID
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Change task status
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/project/{projectId}` - Get tasks by project

### Notification Endpoints
- `GET /api/notifications` - List notifications
- `POST /api/notifications` - Create notification
- `DELETE /api/notifications/{id}` - Delete notification
- `GET /api/notifications/health` - Health check

## 🔧 Configuration

The monolith uses the same configuration as the microservices version but without Kafka/messaging:

- **Database**: PostgreSQL (single database for all services)
- **Cache**: Redis for session storage and caching
- **Authentication**: JWT-based with refresh tokens
- **API Documentation**: OpenAPI/Swagger available at `/swagger-ui.html`

## 🧪 Testing

```bash
# Test health endpoints
curl http://localhost:8080/api/auth/health
curl http://localhost:8080/api/notifications/health
curl http://localhost:8080/actuator/health

# Test API endpoints
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

## 📊 Benefits of Monolith Version

- **Simpler Deployment**: Single Docker container instead of 8+
- **Lower Resource Usage**: No Kafka, Zookeeper, or multiple JVMs
- **Easier Development**: Single codebase to debug and maintain
- **Faster Startup**: No waiting for multiple services to start
- **No Network Latency**: All calls are in-process

## 🔄 Migration Path

The monolith maintains the same API structure as the microservices version, making it easy to:
1. Develop and test features in the monolith
2. Migrate back to microservices when needed
3. Use as a reference implementation

---

**Built with ❤️ for simplified SaaS development**
