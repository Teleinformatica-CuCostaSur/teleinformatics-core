# Use an official Gradle image as the build stage
FROM gradle:8.14.4-jdk17-alpine AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle build files to the container
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Download dependencies (this will be cached if the build files haven't changed)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the source code to the container
COPY src src

# Compile the application
RUN ./gradlew build -x test --no-daemon

# Execute JAR file
FROM eclipse-temurin:17-jre-alpine

# Metadata
LABEL maintainer="Teleinformatics Team"
LABEL description="A Spring Boot application for teleinformatics."
LABEL version="1.0.0"

# Install curl for health checks
RUN apk add --no-cache curl

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership of the application files to the non-root user
RUN chown appuser:appgroup app.jar
USER appuser:appgroup

# Expose the application port
EXPOSE 8080

# JAVA configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]