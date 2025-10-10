
# Use official OpenJDK 17 Alpine image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install Maven (needed to build the app)
RUN apk add --no-cache maven bash

# Copy pom.xml and download dependencies (caching)
COPY pom.xml .

# Pre-download dependencies to speed up builds
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the project without running tests
RUN mvn clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","target/chatapp-0.0.1-SNAPSHOT.jar"]
