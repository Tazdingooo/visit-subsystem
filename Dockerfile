# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Cache dependency resolution: copy wrapper + pom first, resolve, then add sources.
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B -N dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/visit-subsystem-0.0.1-SNAPSHOT.jar app.jar

# Persistent H2 data lives on a mounted volume at /app/data (see compose file).
RUN mkdir -p /app/data

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
