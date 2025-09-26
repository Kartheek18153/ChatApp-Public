# Stage 1: Build the app using official Maven image
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first for dependency caching
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the jar without running tests
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/chatapp-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
