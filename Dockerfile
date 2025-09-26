# ---- Build Stage ----
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set work directory
WORKDIR /app

# Copy pom.xml first to download dependencies
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application without running tests
RUN mvn clean package spring-boot:repackage -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/chatapp-public-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
