FROM openjdk:21-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /producer
COPY build/libs/producer-0.0.1-SNAPSHOT.jar producer.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "producer.jar"]