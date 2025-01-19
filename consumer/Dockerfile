FROM openjdk:21-slim
WORKDIR /consumer
COPY build/libs/consumer-0.0.1-SNAPSHOT.jar consumer.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "consumer.jar"]