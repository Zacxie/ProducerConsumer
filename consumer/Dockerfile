FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY gradle gradle/
COPY settings.gradle ./
COPY common common/
COPY consumer consumer/

RUN gradle :producer:build --no-daemon

FROM openjdk:21-slim
WORKDIR /consumer
COPY --from=builder /app/consumer/build/libs/consumer-0.0.1-SNAPSHOT.jar consumer.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "consumer.jar"]