package com.assessment.producer.controller;

import com.assessment.common.dto.Message;
import com.assessment.producer.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(path = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> getMessageStream() {
        return messageService.getMessageStream();
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
