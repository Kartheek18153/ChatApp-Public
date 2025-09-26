# Use Maven + JDK image to build the app
FROM maven:3.9.2-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the app (skip tests for faster build)
RUN mvn clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/chatapp-0.0.1-SNAPSHOT.jar ./chatapp.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java","-jar","chatapp.jar"]
