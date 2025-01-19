package com.assessment.producer.service;

import com.assessment.common.dto.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService();
    }

    @AfterEach
    void tearDown() throws Exception {
        messageService.close();
    }

    @Test
    void getMessageStream_ShouldUseVirtualThreads() {
        AtomicReference<Boolean> isVirtualThread = new AtomicReference<>();
        AtomicReference<String> threadName = new AtomicReference<>();

        StepVerifier.withVirtualTime(() -> messageService.getMessageStream()
                        .take(1)
                        .doOnNext(message -> {
                            Thread currentThread = Thread.currentThread();
                            threadName.set(currentThread.getName());
                            isVirtualThread.set(currentThread.isVirtual());
                        }))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .expectComplete()
                .verify();

        assertTrue(threadName.get().matches("message-generator\\d+"),
                "Thread name should match the expected pattern");
        assertTrue(isVirtualThread.get(),
                "Message generation should run on a virtual thread");
    }

    @Test
    void getMessageStream_ShouldEmitMessagesWithUniqueIds() {
        Set<String> ids = ConcurrentHashMap.newKeySet();

        StepVerifier.withVirtualTime(() -> messageService.getMessageStream()
                        .take(5)
                        .map(Message::getId)
                        .doOnNext(ids::add))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(5))
                .expectNextCount(5)
                .expectComplete()
                .verify();

        assertEquals(5, ids.size(), "Should generate 5 messages with unique IDs");
    }

    @Test
    void getMessageStream_ShouldGenerateMessagesWithCorrectStringLength() {
        StepVerifier.withVirtualTime(() -> messageService.getMessageStream()
                        .take(1))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(1))
                .assertNext(message -> {
                    assertEquals(8, message.getMessage().length(),
                            "Message content should be 8 characters long");
                    assertTrue(message.getMessage().matches("[a-zA-Z0-9]{8}"),
                            "Message should only contain alphanumeric characters");
                })
                .expectComplete()
                .verify();
    }

    @Test
    void getMessageStream_ShouldEmitMessagesAtCorrectIntervals() {
        StepVerifier.withVirtualTime(() -> messageService.getMessageStream()
                        .take(3))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNextCount(1)
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    void close_ShouldShutdownExecutorServiceGracefully() throws Exception {
        messageService.close();

        StepVerifier.create(messageService.getMessageStream().take(1))
                .expectError()
                .verify(Duration.ofSeconds(2));

        assertDoesNotThrow(() -> messageService.close());
    }
}