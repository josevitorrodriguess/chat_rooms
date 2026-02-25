package com.josevitor.chatrooms.messages;

import com.josevitor.chatrooms.messages.dto.MessageResponse;
import com.josevitor.chatrooms.messages.dto.MessagesPageResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class MessageQueryService {

    private final MessageRepository messageRepository;

    public MessageQueryService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessagesPageResponse fetchPage(Long roomId, String cursor, int limit) {
        List<Message> messages;
        if (cursor == null || cursor.isBlank()) {
            messages = messageRepository.findPage(roomId, limit);
        } else {
            Cursor parsed = parseCursor(cursor);
            messages = messageRepository.findPageBefore(roomId, parsed.createdAt, parsed.id, limit);
        }

        List<MessageResponse> items = messages.stream().map(MessageQueryService::toResponse).toList();
        String nextCursor = null;
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            nextCursor = formatCursor(last.getCreatedAt(), last.getId());
        }
        return new MessagesPageResponse(items, nextCursor);
    }

    private static MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getClientMessageId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private static String formatCursor(Instant createdAt, Long id) {
        return createdAt.toString() + "|" + id;
    }

    private static Cursor parseCursor(String raw) {
        String[] parts = raw.split("\\|", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor");
        }
        try {
            Instant createdAt = Instant.parse(parts[0]);
            Long id = Long.parseLong(parts[1]);
            return new Cursor(createdAt, id);
        } catch (DateTimeParseException | NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private record Cursor(Instant createdAt, Long id) {
    }
}
