FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Install Maven (Alpine needs openjdk + maven)
RUN apk add --no-cache maven bash git

COPY pom.xml .
COPY src ./src

# Build the app
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/chatapp-0.0.1-SNAPSHOT.jar"]
