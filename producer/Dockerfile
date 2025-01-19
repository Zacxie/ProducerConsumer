FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY gradle gradle/
COPY settings.gradle ./
COPY common common/
COPY producer producer/

RUN gradle :producer:build --no-daemon

FROM openjdk:21-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /producer
COPY --from=builder /app/producer/build/libs/producer-0.0.1-SNAPSHOT.jar producer.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "producer.jar"]