# ---- Build Stage ----
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies (offline mode)
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the Spring Boot app (skip tests for faster build)
RUN mvn clean package spring-boot:repackage -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/chatapp-public-0.0.1-SNAPSHOT.jar app.jar

# Expose the default port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java","-jar","app.jar"]
