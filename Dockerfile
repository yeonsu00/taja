FROM --platform=linux/arm64 eclipse-temurin:21-jdk-lunar as builder
WORKDIR /app
COPY gradlew ./
COPY gradle gradle
COPY build.gradle ./
COPY settings.gradle ./
RUN ./gradlew dependencies
COPY src src
RUN ./gradlew build -x test

FROM --platform=linux/arm64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
