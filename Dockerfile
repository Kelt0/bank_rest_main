FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /workspace/app

COPY pom.xml ./
RUN mvn -B -DskipTests=true dependency:go-offline

COPY src ./src
COPY docs/openapi.yaml ./src/main/resources/
RUN mvn -B -DskipTests=true clean package

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder /workspace/app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
