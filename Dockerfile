FROM openjdk:20-jdk-slim
LABEL authors="VanshP"

WORKDIR /app

# Copy the JAR file from host to container
COPY build/libs/*.jar app.jar

# Optional RUN command (e.g., verifying that file exists)
RUN echo "App JAR copied successfully!" && ls -lh app.jar

# Expose Spring Boot default port
EXPOSE 7070

# Run the JAR when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
