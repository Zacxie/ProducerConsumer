FROM openjdk:21-slim
WORKDIR /producer
COPY build/libs/producer-0.0.1-SNAPSHOT.jar producer.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "producer.jar"]