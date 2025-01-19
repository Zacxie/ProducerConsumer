# Spring Boot Project

This repository contains a Spring Boot application with producer and consumer components.

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