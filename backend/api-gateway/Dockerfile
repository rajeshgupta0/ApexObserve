FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl backend/api-gateway -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
