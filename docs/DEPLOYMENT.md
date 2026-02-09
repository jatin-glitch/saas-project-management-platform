# Deployment Guide

This guide covers deploying the Multi-Tenant SaaS Project Management Platform to production environments.

## 🏗️ Architecture Overview

The platform consists of:
- **4 Microservices** (Auth, Project, Notification, Gateway)
- **Frontend SPA** (React + TypeScript)
- **3 Infrastructure Services** (PostgreSQL, Redis, Kafka)
- **Load Balancer** (Nginx)

## 🚀 Production Deployment

### 1. Server Requirements

#### Minimum Specifications
- **CPU**: 4 cores
- **RAM**: 8GB
- **Storage**: 100GB SSD
- **Network**: 100 Mbps

#### Recommended Specifications
- **CPU**: 8 cores
- **RAM**: 16GB
- **Storage**: 250GB SSD
- **Network**: 1 Gbps

### 2. Environment Setup

#### Install Dependencies
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Nginx (if not using container)
sudo apt install nginx -y
```

#### Configure Firewall
```bash
# Allow necessary ports
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

### 3. Application Configuration

#### Create Environment File
```bash
# Copy and edit production environment
cp .env.example .env
nano .env
```

#### Critical Production Settings
```bash
# Database Configuration
POSTGRES_DB=saas_platform_prod
POSTGRES_USER=saas_user_prod
POSTGRES_PASSWORD=your-secure-password-here
DATABASE_URL=jdbc:postgresql://postgres:5432/saas_platform_prod

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com

# Rate Limiting
RATE_LIMIT_RPM=100
```

### 4. Deploy with Docker Compose

#### Production Deployment
```bash
# Deploy all services
docker-compose -f docker-compose.prod.yml up -d

# Verify deployment
docker-compose -f docker-compose.prod.yml ps
```

#### Scale Services (if needed)
```bash
# Scale backend services
docker-compose -f docker-compose.prod.yml up -d --scale auth-service=2 --scale project-service=2
```

### 5. SSL/TLS Configuration

#### Option A: Let's Encrypt (Recommended)
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Generate SSL certificate
sudo certbot --nginx -d yourdomain.com -d app.yourdomain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

#### Option B: Custom Certificates
```bash
# Place certificates in nginx directory
sudo mkdir -p /etc/nginx/ssl
sudo cp your-cert.pem /etc/nginx/ssl/
sudo cp your-key.pem /etc/nginx/ssl/
```

### 6. Nginx Configuration

#### Production Nginx Config
```nginx
# /etc/nginx/sites-available/saas-platform
server {
    listen 80;
    server_name yourdomain.com app.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com app.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # WebSocket
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 7. Database Setup

#### Production PostgreSQL Configuration
```bash
# Connect to PostgreSQL container
docker exec -it saas-postgres-prod psql -U saas_user_prod -d saas_platform_prod

# Create indexes for performance
CREATE INDEX CONCURRENTLY idx_projects_tenant_id ON projects(tenant_id);
CREATE INDEX CONCURRENTLY idx_projects_status ON projects(status);
CREATE INDEX CONCURRENTLY idx_tasks_tenant_id ON tasks(tenant_id);
CREATE INDEX CONCURRENTLY idx_tasks_project_id ON tasks(project_id);
CREATE INDEX CONCURRENTLY idx_tasks_status ON tasks(status);

# Analyze tables for query optimization
ANALYZE projects;
ANALYZE tasks;
ANALYZE users;
```

#### Backup Strategy
```bash
# Create backup script
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
mkdir -p $BACKUP_DIR

# Database backup
docker exec saas-postgres-prod pg_dump -U saas_user_prod saas_platform_prod | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# Keep last 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete
EOF

chmod +x backup.sh

# Schedule daily backups
echo "0 2 * * * /path/to/backup.sh" | crontab -
```

## ☁️ Cloud Deployment

### AWS Deployment

#### ECS (Elastic Container Service)
```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name saas-platform

# Create task definition
aws ecs register-task-definition --cli-input-json file://task-definition.json

# Create service
aws ecs create-service \
  --cluster saas-platform \
  --service-name saas-platform-service \
  --task-definition saas-platform-task \
  --desired-count 2
```

#### RDS (Relational Database Service)
```bash
# Create PostgreSQL subnet group
aws rds create-db-subnet-group \
  --db-subnet-group-name saas-platform-sg \
  --subnet-ids subnet-12345678 subnet-87654321

# Create PostgreSQL instance
aws rds create-db-instance \
  --db-instance-identifier saas-platform-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --master-username saas_user \
  --master-user-password your-secure-password \
  --allocated-storage 100 \
  --vpc-security-group-ids sg-12345678 \
  --db-subnet-group-name saas-platform-sg
```

#### ElastiCache (Redis)
```bash
# Create Redis subnet group
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name saas-platform-redis-sg \
  --subnet-ids subnet-12345678 subnet-87654321

# Create Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id saas-platform-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --security-group-ids sg-12345678 \
  --cache-subnet-group-name saas-platform-redis-sg
```

### Google Cloud Platform

#### GKE (Google Kubernetes Engine)
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: saas-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: saas-platform
  template:
    metadata:
      labels:
        app: saas-platform
    spec:
      containers:
      - name: auth-service
        image: gcr.io/your-project/auth-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
```

#### Cloud SQL
```bash
# Create Cloud SQL instance
gcloud sql instances create saas-platform-db \
  --database-version=POSTGRES_15 \
  --tier=db-n1-standard-2 \
  --region=us-central1 \
  --storage-size=100GB

# Create database
gcloud sql databases create saas_platform \
  --instance=saas-platform-db
```

### Microsoft Azure

#### AKS (Azure Kubernetes Service)
```bash
# Create resource group
az group create --name saas-platform-rg --location eastus

# Create AKS cluster
az aks create \
  --resource-group saas-platform-rg \
  --name saas-platform-cluster \
  --node-count 3 \
  --node-vm-size Standard_D2s_v3 \
  --enable-addons monitoring
```

#### Azure Database
```bash
# Create PostgreSQL server
az postgres server create \
  --name saas-platform-db \
  --resource-group saas-platform-rg \
  --location eastus \
  --admin-user saas_user \
  --admin-password your-secure-password \
  --sku-name B_Gen5_2 \
  --version 15
```

## 📊 Monitoring & Logging

### Application Monitoring

#### Prometheus + Grafana
```yaml
# monitoring/docker-compose.yml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

#### Log Aggregation
```yaml
# logging/docker-compose.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.5.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.5.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

### Health Checks

#### Application Health Endpoints
```bash
# Health check script
cat > health-check.sh << 'EOF'
#!/bin/bash
SERVICES=("http://localhost:8080/actuator/health"
           "http://localhost:8081/api/auth/health"
           "http://localhost:8082/actuator/health"
           "http://localhost:8083/actuator/health")

for service in "${SERVICES[@]}"; do
    response=$(curl -s -o /dev/null -w "%{http_code}" $service)
    if [ $response -eq 200 ]; then
        echo "✅ $service is healthy"
    else
        echo "❌ $service is unhealthy (HTTP $response)"
    fi
done
EOF

chmod +x health-check.sh
```

## 🔒 Security Hardening

### Network Security
```bash
# Configure fail2ban
sudo apt install fail2ban -y
sudo systemctl enable fail2ban

# Configure rate limiting in nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
limit_req_zone $binary_remote_addr zone=auth:10m rate=5r/s;
```

### Application Security
```bash
# Security headers in Nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-Frame-Options "DENY" always;
add_header X-XSS-Protection "1; mode=block" always;
```

### Database Security
```sql
-- Create read-only user for reporting
CREATE USER reporting_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE saas_platform_prod TO reporting_user;
GRANT USAGE ON SCHEMA public TO reporting_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO reporting_user;

-- Row Level Security
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON projects FOR ALL TO saas_user_prod 
USING (tenant_id = current_setting('app.current_tenant_id')::integer);
```

## 🚀 Performance Optimization

### Database Optimization
```sql
-- Connection pooling settings in application-prod.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### Caching Strategy
```bash
# Redis configuration for production
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=5
spring.cache.redis.time-to-live=600000
```

### CDN Configuration
```nginx
# Static asset caching
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
    add_header X-CDN-Cache "HIT";
}
```

## 🔄 CI/CD Pipeline

### Automated Deployment
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Deploy to server
      uses: appleboy/ssh-action@v0.1.5
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        key: ${{ secrets.SSH_KEY }}
        script: |
          cd /opt/saas-platform
          git pull origin main
          docker-compose -f docker-compose.prod.yml down
          docker-compose -f docker-compose.prod.yml up -d --build
```

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Environment variables configured
- [ ] SSL certificates obtained
- [ ] Database backup strategy in place
- [ ] Monitoring tools configured
- [ ] Security groups configured
- [ ] Load testing performed

### Post-Deployment
- [ ] Health checks passing
- [ ] SSL certificates valid
- [ ] Monitoring alerts configured
- [ ] Backup verification
- [ ] Performance benchmarks recorded
- [ ] Security scan completed

---

For additional deployment support, refer to the main [README.md](../README.md) or create an issue.
