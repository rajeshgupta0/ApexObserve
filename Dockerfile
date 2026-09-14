# Multi-Service Root Dockerfile for Railway Auto-Provisioning
# This single file builds the entire monorepo, then dynamically runs the correct service jar
# based on the automatically injected RAILWAY_SERVICE_NAME environment variable.

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy source code
COPY . .

# Build all Maven modules in the monorepo
RUN mvn clean package -DskipTests

# Gather all executable jars into a flat directory, excluding sources/javadoc/original thin jars
RUN mkdir -p /app/all-jars && \
    find backend demo-services -type f -name "*.jar" ! -name "*.original" ! -name "*javadoc*" ! -name "*sources*" -exec cp {} /app/all-jars/ \;

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy all pre-built executable JARs from the build stage
COPY --from=build /app/all-jars/ /app/all-jars/

# Set default port (Railway automatically injects $PORT if public, but 8080 is our internal default)
ENV PORT=8080
EXPOSE 8080

# Railway automatically injects RAILWAY_SERVICE_NAME based on the service name in the dashboard.
# We find the specific executable JAR matching the service name (e.g., api-gateway-1.0.0.jar) and run it.
ENTRYPOINT ["sh", "-c", "TARGET_JAR=$(find /app/all-jars -name \"${RAILWAY_SERVICE_NAME}-[0-9]*.jar\" | head -n 1); if [ -z \"$TARGET_JAR\" ]; then echo \"ERROR: Could not find JAR for service: ${RAILWAY_SERVICE_NAME}\"; exit 1; fi; echo \"Starting $TARGET_JAR\"; exec java -Dserver.port=${PORT} -jar \"$TARGET_JAR\""]
