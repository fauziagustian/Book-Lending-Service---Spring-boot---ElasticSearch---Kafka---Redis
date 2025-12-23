# Multi-stage build so you only need Docker to run locally
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests=false test package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/book-lending-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
