package com.josevitor.chatrooms.messages.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        Long id,
        Long roomId,
        Long senderId,
        UUID clientMessageId,
        String content,
        Instant createdAt
) {
}
