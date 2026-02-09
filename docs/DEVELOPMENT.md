# Development Setup Guide

This guide will help you set up the development environment for the Multi-Tenant SaaS Project Management Platform.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21+** (OpenJDK recommended)
- **Maven 3.9+**
- **Node.js 18+** and npm
- **Docker** and **Docker Compose**
- **Git**

## 1. Clone the Repository

```bash
git clone <repository-url>
cd saas-project
```

## 2. Environment Setup

### Backend Environment

1. **Copy environment template**
   ```bash
   cp backend/src/main/resources/application-dev.properties.example \
      backend/src/main/resources/application-dev.properties
   ```

2. **Update database credentials** in `application-dev.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/saas_platform_dev
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password
   ```

### Frontend Environment

1. **Copy environment template**
   ```bash
   cp frontend/.env.example frontend/.env
   ```

2. **Update API URL** in `frontend/.env`:
   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```

## 3. Start Infrastructure Services

### Option A: Using Docker Compose (Recommended)

```bash
# Start only infrastructure services
docker-compose up postgres redis kafka -d

# Verify services are running
docker-compose ps
```

### Option B: Manual Installation

#### PostgreSQL
```bash
# Install PostgreSQL 15
sudo apt-get install postgresql-15 postgresql-client-15

# Create database and user
sudo -u postgres psql
CREATE DATABASE saas_platform_dev;
CREATE USER saas_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE saas_platform_dev TO saas_user;
```

#### Redis
```bash
# Install Redis
sudo apt-get install redis-server

# Start Redis
sudo systemctl start redis-server
```

#### Kafka
```bash
# Download and extract Kafka
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties &

# Start Kafka
bin/kafka-server-start.sh config/server.properties &
```

## 4. Backend Development Setup

### Build All Services
```bash
cd backend
mvn clean install -DskipTests
```

### Run Individual Services

Open separate terminal windows for each service:

#### Auth Service (Port 8081)
```bash
cd backend/auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Project Service (Port 8082)
```bash
cd backend/project-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Notification Service (Port 8083)
```bash
cd backend/notification-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Gateway (Port 8080)
```bash
cd backend/gateway
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Verify Backend Services

```bash
# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/api/auth/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

## 5. Frontend Development Setup

### Install Dependencies
```bash
cd frontend
npm install
```

### Start Development Server
```bash
npm run dev
```

The frontend will be available at: http://localhost:3000

## 6. Database Setup

### Initialize Database Schema

The application will automatically create/update the schema on startup due to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Seed Data (Optional)

Create a SQL file `backend/src/main/resources/data.sql`:

```sql
-- Insert sample tenants
INSERT INTO tenants (id, name, domain, created_at) VALUES 
(1, 'Demo Tenant', 'demo.example.com', NOW()),
(2, 'Test Tenant', 'test.example.com', NOW());

-- Insert sample users
INSERT INTO users (id, email, password, first_name, last_name, role, tenant_id, created_at) VALUES
(UUID_GENERATE(), 'admin@demo.com', '$2a$10$...', 'Admin', 'User', 'SUPER_ADMIN', 1, NOW()),
(UUID_GENERATE(), 'manager@demo.com', '$2a$10$...', 'Manager', 'User', 'PROJECT_MANAGER', 1, NOW());
```

## 7. Testing the Setup

### Create Test User

Use the following credentials to test:

- **Tenant ID**: 1
- **Email**: admin@demo.com
- **Password**: password123

### API Testing

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 1" \
  -d '{"email":"admin@demo.com","password":"password123"}'

# Create Project (replace TOKEN with actual JWT)
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -H "X-Tenant-ID: 1" \
  -d '{"name":"Test Project","description":"A test project","code":"TEST-001","status":"PLANNING","priority":"MEDIUM","isPublic":false,"tags":[]}'
```

## 8. Development Tools

### IDE Configuration

#### VS Code
Install these extensions:
- Java Extension Pack
- Spring Boot Extension Pack
- ES7+ React/Redux/React-Native snippets
- TypeScript Importer
- Prettier - Code formatter

#### IntelliJ IDEA
1. Enable Spring Boot plugin
2. Configure database connection
3. Set up code style for Java and TypeScript

### Debugging

#### Backend Debugging
```bash
# Start with debug enabled
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

#### Frontend Debugging
The development server includes source maps and hot reload by default.

## 9. Common Development Issues

### Port Conflicts
If ports are already in use:
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Database Connection Issues
1. Verify PostgreSQL is running
2. Check connection string in application-dev.properties
3. Ensure database and user exist
4. Check firewall settings

### Frontend Build Issues
```bash
# Clear node modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Backend Compilation Issues
```bash
# Clean and rebuild
mvn clean install -DskipTests
```

## 10. Development Workflow

### Daily Development
1. Pull latest changes: `git pull origin develop`
2. Start infrastructure: `docker-compose up postgres redis kafka -d`
3. Run backend services in separate terminals
4. Start frontend development server
5. Make changes and test locally
6. Run tests before committing

### Code Quality
```bash
# Backend linting
mvn spotless:check

# Frontend linting
cd frontend && npm run lint

# Run tests
mvn test
cd frontend && npm test
```

### Commit Standards
```bash
# Format code
mvn spotless:apply
cd frontend && npm run lint:fix

# Commit with conventional messages
git commit -m "feat: add user authentication"
git commit -m "fix: resolve database connection issue"
```

## 11. Troubleshooting

### Service Not Starting
1. Check logs for error messages
2. Verify all dependencies are running
3. Check port availability
4. Validate configuration files

### Database Issues
1. Verify database is accessible
2. Check connection parameters
3. Review SQL logs
4. Test with a database client

### Frontend Issues
1. Clear browser cache
2. Check network tab in browser dev tools
3. Verify API endpoints are accessible
4. Review console for JavaScript errors

## 12. Performance Monitoring

### Local Monitoring
- **Spring Boot Actuator**: http://localhost:8080/actuator
- **H2 Console** (if using H2): http://localhost:8080/h2-console
- **Redis CLI**: `redis-cli`
- **Kafka Tools**: Kafka monitoring tools

### Profiling
- **Java**: Use VisualVM or JProfiler
- **Node.js**: Use Chrome DevTools Profiler
- **Database**: Use EXPLAIN ANALYZE for slow queries

---

For additional help, refer to the main [README.md](../README.md) or create an issue.
