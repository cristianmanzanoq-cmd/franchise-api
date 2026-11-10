FROM gradle:8.7-jdk17-alpine AS build
WORKDIR /workspace

COPY settings.gradle build.gradle gradle.properties lombok.config ./
COPY domain ./domain
COPY infrastructure ./infrastructure
COPY applications ./applications

RUN gradle :app-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app
COPY --from=build /workspace/applications/app-service/build/libs/franchise-api.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
