package com.wedding.backend.message.api;

import com.wedding.backend.message.api.MessageDtos.CreateMessageRequest;
import com.wedding.backend.message.api.MessageDtos.MessageResponse;
import com.wedding.backend.message.model.Message;
import com.wedding.backend.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(@Valid @RequestBody CreateMessageRequest request) {
        Message message = messageService.createMessage(request);
        return MessageResponse.fromEntity(message);
    }
}

