# Use Temurin JDK
FROM eclipse-temurin:17-jdk-alpine

# Install bash, maven, and git
RUN apk add --no-cache bash maven git

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "target/chatapp-0.0.1-SNAPSHOT.jar"]
