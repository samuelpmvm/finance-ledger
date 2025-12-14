# JDK base image
FROM maven:3.9.6-eclipse-temurin-21 AS builder
LABEL authors="samarcos"

WORKDIR /app

# Copy and build the project
COPY . .
RUN mvn -B -DskipTests clean package

# Final image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar finance-ledger-app.jar

# Expose the application port
EXPOSE 8080

# Set the entry point for the application
ENTRYPOINT ["java", "-jar", "finance-ledger-app.jar"]