# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and POM (if using mvnw)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies offline to cache
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the project without running tests
RUN ./mvnw clean package -DskipTests -B

# Expose port 8080
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "target/chatapp-0.0.1-SNAPSHOT.jar"]
