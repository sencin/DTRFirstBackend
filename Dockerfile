# ---------- Stage 1: Build the JAR ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven files first (for better cache)
COPY pom.xml .
COPY src ./src

# Package the app (skip tests)
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run the app ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]