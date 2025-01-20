# Spring Boot Project

This monorepo contains two independent modules built with Spring Boot and Project Reactor (Flux):

- Producer: A Spring Boot application that generates and sends messages
- Consumer: A Spring Boot application that subscribes to messages from the Producer and saves them to /tmp/consumer within its Docker container

Both modules are built using Gradle and leverage Spring WebFlux for concurrent processing.

## Requirements

Before running this project, ensure you have the following prerequisites installed:

- Docker
- Docker Compose

## Running the Application

You can run this application using either Docker Compose or Docker directly.

### Using Docker Compose

This is the recommended method as it handles all component dependencies automatically.

1. Navigate to the project root directory
2. Run the following command:
   ```bash
   docker-compose up
   ```

### Using Docker

Alternatively, you can build and run each component separately using Docker.

#### Producer

1. Build the producer image:
   ```bash
   docker build -t producer -f producer/Dockerfile .
   ```

2. Run the producer container:
   ```bash
   docker run -it producer
   ```

#### Consumer

1. Build the consumer image:
   ```bash
   docker build -t consumer -f consumer/Dockerfile .
   ```

2. Run the consumer container:
   ```bash
   docker run -it producer
   ```

   > **Note:** Make sure the producer is running before starting the consumer to ensure proper communication between components.