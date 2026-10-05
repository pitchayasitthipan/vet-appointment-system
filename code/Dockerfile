# ==========================================
# Stage 1: Build Stage using Maven & JDK 17
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy Maven POM and Source Code
COPY pom.xml .
COPY src ./src

# Package application JAR (skip tests during Docker build)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime Stage using Lightweight JRE
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot HTTP Port
EXPOSE 8080

# Environment configurations
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Execute application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
