# Multi-Tenant SaaS Project Management Platform

A production-ready, multi-tenant SaaS platform for project and task management with real-time notifications, built with modern microservices architecture.

## 🏗️ Architecture Overview

### Backend Services
- **Auth Service** - Authentication, authorization, JWT token management
- **Project Service** - Project CRUD operations, management, and statistics
- **Notification Service** - Real-time notifications and event handling
- **Gateway** - API Gateway with routing and load balancing
- **Common Library** - Shared utilities, entities, and configurations

### Frontend
- **React + TypeScript** - Modern, type-safe frontend application
- **Zustand** - Lightweight state management
- **Tailwind CSS** - Utility-first CSS framework
- **React Query** - Server state management and caching

### Infrastructure
- **PostgreSQL** - Primary database with multi-tenant support
- **Redis** - Caching and session storage
- **Kafka** - Event streaming and message queuing
- **Nginx** - Reverse proxy and static file serving

## 🚀 Features

### Core Functionality
- ✅ Multi-tenant architecture with tenant isolation
- ✅ JWT-based authentication with refresh tokens
- ✅ Role-based access control (RBAC)
- ✅ Project management with full CRUD operations
- ✅ Task management with assignment and tracking
- ✅ Real-time notifications via WebSocket
- ✅ Audit logging for all operations
- ✅ Rate limiting and security headers
- ✅ Comprehensive API documentation (OpenAPI)

### Technical Features
- ✅ Docker containerization for all services
- ✅ Docker Compose for local development
- ✅ CI/CD pipeline with GitHub Actions
- ✅ Integration tests with Testcontainers
- ✅ Environment-specific configurations
- ✅ Health checks and monitoring
- ✅ Security scanning and vulnerability detection
- ✅ Automated dependency updates

## 📋 Prerequisites

- **Java 21+**
- **Node.js 18+**
- **Docker & Docker Compose**
- **Maven 3.9+**
- **PostgreSQL 15+** (for local development)
- **Redis 7+** (for local development)

## 🛠️ Quick Start

### Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd saas-project
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:80
   - API Gateway: http://localhost:8080
   - Auth Service: http://localhost:8081
   - Project Service: http://localhost:8082
   - Notification Service: http://localhost:8083

### Local Development

1. **Start infrastructure services**
   ```bash
   docker-compose up postgres redis kafka -d
   ```

2. **Build and run backend services**
   ```bash
   cd backend
   mvn clean install
   # Run each service in separate terminals
   cd auth-service && mvn spring-boot:run
   cd project-service && mvn spring-boot:run
   cd notification-service && mvn spring-boot:run
   cd gateway && mvn spring-boot:run
   ```

3. **Start frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## 🔧 Configuration

### Environment Variables

Create a `.env` file based on `.env.example`:

```bash
# Database
POSTGRES_DB=saas_platform
POSTGRES_USER=saas_user
POSTGRES_PASSWORD=your-secure-password

# JWT
JWT_SECRET=your-super-secure-jwt-secret-key
JWT_EXPIRATION=86400000

# Application
SPRING_PROFILES_ACTIVE=dev
```

### Profiles

- **dev** - Development with debug logging and relaxed security
- **staging** - Staging environment with production-like settings
- **prod** - Production with optimized performance and security

## 🧪 Testing

### Unit Tests
```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

### Integration Tests
```bash
cd backend/integration-tests
mvn verify
```

### Test Coverage
- Backend: Target >80% code coverage
- Frontend: Target >85% code coverage

## 📊 Monitoring & Health

### Health Endpoints
- Gateway: `GET /actuator/health`
- Auth Service: `GET /api/auth/health`
- Project Service: `GET /actuator/health`
- Notification Service: `GET /actuator/health`

### Metrics
- Prometheus metrics available at `/actuator/prometheus`
- Custom metrics for business KPIs

## 🔒 Security

### Authentication Flow
1. User logs in with email/password and tenant ID
2. Service validates credentials and returns JWT tokens
3. Frontend stores tokens and includes them in API requests
4. Gateway validates JWT tokens on each request
5. Automatic token refresh when access token expires

### Security Features
- JWT token-based authentication
- Rate limiting on authentication endpoints
- CORS configuration for cross-origin requests
- Security headers (XSS, CSRF, Content-Type)
- Input validation and sanitization
- Multi-tenant data isolation

## 🚀 Deployment

### Production Deployment

1. **Prepare environment**
   ```bash
   cp .env.example .env
   # Update .env with production values
   ```

2. **Deploy with Docker Compose**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

### Cloud Deployment Options

#### AWS
- **ECS/EKS** for container orchestration
- **RDS** for managed PostgreSQL
- **ElastiCache** for managed Redis
- **MSK** for managed Kafka
- **Application Load Balancer** for traffic distribution

#### Google Cloud
- **GKE** for Kubernetes orchestration
- **Cloud SQL** for managed PostgreSQL
- **Memorystore** for managed Redis
- **Pub/Sub** for messaging
- **Cloud Load Balancer** for traffic

#### Azure
- **AKS** for Kubernetes
- **Azure Database** for PostgreSQL
- **Redis Cache** for managed Redis
- **Event Hubs** for messaging
- **Application Gateway** for routing

## 📈 Scaling

### Horizontal Scaling
- Stateless services allow easy horizontal scaling
- Database connection pooling for high concurrency
- Redis clustering for cache scaling
- Kafka partitioning for message throughput

### Performance Optimizations
- Database indexing for query performance
- Redis caching for frequently accessed data
- Async processing for non-blocking operations
- CDN for static asset delivery

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow
1. **Code Quality**
   - Linting and formatting checks
   - Security vulnerability scanning
   - Dependency vulnerability analysis

2. **Testing**
   - Unit tests execution
   - Integration tests with Testcontainers
   - Test coverage reporting

3. **Build & Deploy**
   - Docker image building
   - Container registry publishing
   - Environment-specific deployment

### Automated Updates
- Weekly dependency update checks
- Automated pull requests for updates
- Security patch notifications

## 🛠️ Development Guide

### Adding New Features
1. Create feature branch from `develop`
2. Implement backend service changes
3. Add corresponding frontend components
4. Write unit and integration tests
5. Update documentation
6. Submit pull request

### Code Standards
- **Backend**: Follow Java/Spring Boot conventions
- **Frontend**: Use TypeScript strict mode
- **Testing**: Maintain >80% coverage
- **Documentation**: Update API docs for new endpoints

### Database Migrations
- Use Flyway for schema changes
- Write forward and rollback migrations
- Test migrations on sample data

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/logout` - User logout
- `GET /api/auth/validate` - Token validation

### Project Endpoints
- `GET /api/projects` - List projects (paginated)
- `POST /api/projects` - Create project
- `GET /api/projects/{id}` - Get project by ID
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Task Endpoints
- `GET /api/tasks` - List tasks (paginated)
- `POST /api/tasks` - Create task
- `GET /api/tasks/{id}` - Get task by ID
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Change task status

Full API documentation available at: `http://localhost:8080/swagger-ui.html`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the [Wiki](wiki) for detailed guides
- Review existing [Issues](issues) for similar problems

## 🎯 Roadmap

### Upcoming Features
- [ ] Advanced project analytics dashboard
- [ ] File attachment support for tasks
- [ ] Team collaboration features
- [ ] Mobile application (React Native)
- [ ] Advanced reporting and exports
- [ ] Third-party integrations (Slack, Teams, etc.)

### Technical Improvements
- [ ] GraphQL API support
- [ ] Event sourcing architecture
- [ ] Advanced caching strategies
- [ ] Performance monitoring dashboard
- [ ] Automated disaster recovery

---

**Built with ❤️ for modern SaaS development**
