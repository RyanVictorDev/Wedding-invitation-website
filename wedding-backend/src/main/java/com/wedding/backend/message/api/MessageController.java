package com.wedding.backend.message.api;

import com.wedding.backend.common.RateLimiterService;
import com.wedding.backend.message.api.MessageDtos.CreateMessageRequest;
import com.wedding.backend.message.api.MessageDtos.MessageResponse;
import com.wedding.backend.message.api.MessageDtos.PagedMessagesResponse;
import com.wedding.backend.message.model.Message;
import com.wedding.backend.message.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final RateLimiterService rateLimiterService;

    public MessageController(MessageService messageService, RateLimiterService rateLimiterService) {
        this.messageService = messageService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createMessage(@Valid @RequestBody CreateMessageRequest request,
                                         HttpServletRequest httpRequest) {
        // Honeypot: if website is filled, treat as spam
        if (request.website() != null && !request.website().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Spam detected");
        }

        String clientIp = extractClientIp(httpRequest);
        if (!rateLimiterService.isAllowed(clientIp)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests from this IP. Please wait a few seconds.");
        }

        Message message = messageService.createMessage(request);
        return MessageResponse.fromEntity(message);
    }

    @GetMapping
    public PagedMessagesResponse listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return messageService.listMessages(page, size);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // In case of multiple values, take the first one
            int commaIndex = forwarded.indexOf(',');
            return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}

