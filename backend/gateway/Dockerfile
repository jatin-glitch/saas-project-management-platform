# Gateway Dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml files
COPY pom-minimal.xml ./pom.xml
COPY gateway/pom.xml ./gateway/
COPY common-lib/pom.xml ./common-lib/

# Copy common-lib source first
COPY common-lib/src ./common-lib/src

# Install minimal parent POM and build common-lib
RUN mvn clean install -DskipTests

# Download service dependencies
RUN mvn -f gateway/pom.xml dependency:go-offline -B

# Copy service source code
COPY gateway/src ./gateway/src

# Build the application
RUN mvn -f gateway/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser

# Copy the built jar
COPY --from=builder /app/gateway/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}", "app.jar"]
