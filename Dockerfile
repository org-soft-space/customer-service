# Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN  chmod +x ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew bootJar --no-daemon

# Runtime stage

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
