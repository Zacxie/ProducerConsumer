package com.assessment.consumer.service;

import com.assessment.common.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MessageConsumerService implements InitializingBean, AutoCloseable {

    private final WebClient webClient;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;
    private final Path baseStoragePath;
    private volatile boolean running = true;
    private Disposable subscription;

    public MessageConsumerService(
            WebClient.Builder webClientBuilder,
            @Value("${producer.url}") String producerUrl,
            ObjectMapper objectMapper,
            @Value("${storage.path:./message-storage}") String storagePath
    ) {
        Thread.Builder.OfVirtual virtualBuilder = Thread.ofVirtual().name("message-consumer", 0);
        this.executorService = Executors.newThreadPerTaskExecutor(virtualBuilder.factory());
        this.webClient = webClientBuilder.baseUrl(producerUrl).build();
        this.objectMapper = objectMapper;
        this.baseStoragePath = Paths.get(storagePath);
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(baseStoragePath);
        } catch (Exception e) {
            log.error("Failed to create storage directory", e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        startConsuming();
    }

    private void startConsuming() {
        subscription = webClient.get()
                .uri("/api/v1/message")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Message.class)
                .publishOn(Schedulers.fromExecutor(executorService))
                .doOnNext(this::processMessage)
                .doOnError(e -> log.error("Error consuming message", e))
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .doBeforeRetry(s -> log.warn("Retrying to consume message...")))
                .subscribe();
    }

    private void processMessage(Message message) {
        if (!running) {
            return;
        }

        try {
            writeMessageToFile(message);
            log.info("Processed message: {}", message);
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void writeMessageToFile(Message message) {
        try {
            String dateDir = LocalDateTime.now().toLocalDate().toString();
            Path dailyDir = baseStoragePath.resolve(dateDir);
            Files.createDirectories(dailyDir);

            String filename = String.format("%s-%s.json",
                    LocalDateTime.now().toString().replace(":", "-"),
                    message.getId());
            Path filePath = dailyDir.resolve(filename);

            String json = objectMapper.writeValueAsString(message);
            Files.writeString(filePath, json);

            log.info("Message written to file: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to write message to file", e);
        }
    }

    @Override
    public void close() throws InterruptedException {
        running = false;
        if (subscription != null) {
            subscription.dispose();
        }
        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }
}
