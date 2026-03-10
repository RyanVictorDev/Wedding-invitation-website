package com.wedding.backend.message.api;

import com.wedding.backend.message.model.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class MessageDtos {

    private MessageDtos() {
    }

    public record CreateMessageRequest(
            @NotBlank
            @Size(max = 10000)
            String content,

            @Size(max = 150)
            String authorName
    ) {
    }

    public record MessageResponse(
            Long id,
            String content,
            String authorName,
            OffsetDateTime createdAt
    ) {
        public static MessageResponse fromEntity(Message message) {
            return new MessageResponse(
                    message.getId(),
                    message.getContent(),
                    message.getAuthorName(),
                    message.getCreatedAt()
            );
        }
    }
}

