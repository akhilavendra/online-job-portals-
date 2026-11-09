FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/target/online-job-portal-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /var/data/uploads && chown -R spring:spring /app /var/data/uploads
USER spring:spring
EXPOSE 8080
ENV PORT=8080
ENV UPLOAD_DIR=/var/data/uploads
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]