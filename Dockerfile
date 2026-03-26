FROM --platform=linux/arm64 eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew ./
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle ./
COPY settings.gradle ./
COPY apps/taja-api/build.gradle apps/taja-api/
RUN ./gradlew :apps:taja-api:dependencies
COPY apps/taja-api/src apps/taja-api/src
RUN ./gradlew :apps:taja-api:build -x test

FROM --platform=linux/arm64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/apps/taja-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
