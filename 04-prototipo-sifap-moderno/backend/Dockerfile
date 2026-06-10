# Build (Java 21 + Maven) — multi-stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -e dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/target/sifap-backend-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
