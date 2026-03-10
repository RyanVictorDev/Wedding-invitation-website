package com.wedding.backend.message.service;

import com.wedding.backend.message.api.MessageDtos.CreateMessageRequest;
import com.wedding.backend.message.api.MessageDtos.MessageResponse;
import com.wedding.backend.message.api.MessageDtos.PagedMessagesResponse;
import com.wedding.backend.message.model.Message;
import com.wedding.backend.message.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public PagedMessagesResponse listMessages(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> result = messageRepository.findAll(pageable);
        return new PagedMessagesResponse(
                result.map(MessageResponse::fromEntity).getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}

