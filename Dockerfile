# Use official Maven image with Java 17 for build
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download dependencies without building
RUN mvn dependency:go-offline

# Copy all source code
COPY src ./src

# Build the application (skip tests to speed up)
RUN mvn clean package -DskipTests

# Use a smaller Java runtime image for running the app
FROM eclipse-temurin:17-jdk-jammy

# Set working directory in runtime container
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/chatapp-0.0.1-SNAPSHOT.jar ./chatapp.jar

# Expose Spring Boot default port
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java","-jar","chatapp.jar"]
