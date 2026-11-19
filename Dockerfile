# Stage 1: Build the application with Maven and Java 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven POM and source files
COPY pom.xml .
COPY frontend ./frontend
COPY src ./src

# Install Maven in build container
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Package application with production Vaadin frontend
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the generated JAR from build stage
COPY --from=build /app/target/studyflow.jar app.jar

# Render dynamic port environment variable
ENV PORT=8080
EXPOSE 8080

# Optimize JVM memory flags for Render Free Tier (512MB memory limit)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xmx384m -Dserver.port=${PORT} -jar app.jar"]
