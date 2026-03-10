package com.wedding.backend.message.service;

import com.wedding.backend.message.api.MessageDtos.CreateMessageRequest;
import com.wedding.backend.message.model.Message;
import com.wedding.backend.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message createMessage(CreateMessageRequest request) {
        Message message = new Message();
        message.setContent(request.content());
        message.setAuthorName(request.authorName());
        return messageRepository.save(message);
    }
}

