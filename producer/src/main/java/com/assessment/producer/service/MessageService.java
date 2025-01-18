package com.assessment.producer.service;

import com.assessment.common.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MessageService implements AutoCloseable {
    private final ExecutorService executorService;
    private final Random random = new Random();

    public MessageService() {
        Thread.Builder.OfVirtual virtualBuilder = Thread.ofVirtual().name("message-generator", 0);
        this.executorService = Executors.newThreadPerTaskExecutor(virtualBuilder.factory());
    }

    public Flux<Message> getMessageStream() {
        log.info("Starting message stream...");

        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> Flux.defer(() ->
                        Mono.fromCallable(this::generateMessage)
                                .subscribeOn(Schedulers.fromExecutor(executorService))
                ));
    }

    private Message generateMessage() {
        log.info("Generating message on thread: {}", Thread.currentThread());

        return Message.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .message(generateRandomString(8))
                .build();
    }

    private String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }

        return result.toString();
    }


    @Override
    public void close() throws Exception {
        log.info("Shutting down message service...");
        if (executorService != null) {
            executorService.shutdown();
            if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }
}
