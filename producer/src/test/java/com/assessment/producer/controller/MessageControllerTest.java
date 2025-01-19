package com.assessment.producer.controller;

import com.assessment.common.dto.Message;
import com.assessment.producer.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MessageController messageController = new MessageController(messageService);
        webTestClient = WebTestClient
                .bindToController(messageController)
                .build();
    }

    @Test
    void health_ShouldReturnOK() {
        webTestClient.get()
                .uri("/api/v1/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("OK");
    }

    @Test
    void getMessageStream_ShouldReturnServerSentEvents() {
        Message message1 = new Message("1", LocalDateTime.of(2025,1,18,12,12), "test1");
        Message message2 = new Message("2", LocalDateTime.of(2025,1,18,12,12), "test2");

        when(messageService.getMessageStream())
                .thenReturn(Flux.just(message1, message2)
                        .delayElements(Duration.ofMillis(100)));

        Flux<Message> responseBody = webTestClient.get()
                .uri("/api/v1/message")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(Message.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .expectNext(message1)
                .expectNext(message2)
                .expectComplete()
                .verify(Duration.ofSeconds(2));
    }

}