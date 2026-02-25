package com.josevitor.chatrooms.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josevitor.chatrooms.messages.dto.MessageResponse;
import com.josevitor.chatrooms.messages.dto.SendMessageRequest;
import com.josevitor.chatrooms.rooms.RoomMemberId;
import com.josevitor.chatrooms.rooms.RoomMemberRepository;
import com.josevitor.chatrooms.rooms.PresenceService;
import com.josevitor.chatrooms.ws.WebSocketPrincipal;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;

import java.security.Principal;
import java.util.Set;

@Controller
public class MessageController {

    private final MessageRepository messageRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final MessageBroadcastService broadcastService;
    private final PresenceService presenceService;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator;
    private final int messagesPerSecond;

    public MessageController(MessageRepository messageRepository,
                             RoomMemberRepository roomMemberRepository,
                             MessageBroadcastService broadcastService,
                             PresenceService presenceService,
                             RateLimitService rateLimitService,
                             @Value("${app.rate-limit.messages-per-second:5}") int messagesPerSecond,
                             Validator validator) {
        this.messageRepository = messageRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.broadcastService = broadcastService;
        this.presenceService = presenceService;
        this.rateLimitService = rateLimitService;
        this.validator = validator;
        this.messagesPerSecond = messagesPerSecond;
    }

    @MessageMapping("/send-message")
    public void sendMessage(@Payload String raw, Principal principal) {
        if (!(principal instanceof WebSocketPrincipal ws)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        SendMessageRequest request;
        try {
            request = objectMapper.readValue(raw, SendMessageRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid payload");
        }

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Validation failed");
        }

        RoomMemberId memberId = new RoomMemberId(request.roomId(), ws.userId());
        if (!roomMemberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("User not in room");
        }

        if (!rateLimitService.allow(ws.userId(), messagesPerSecond)) {
            throw new IllegalArgumentException("Rate limit exceeded");
        }

        Message message = new Message();
        message.setRoomId(request.roomId());
        message.setSenderId(ws.userId());
        message.setClientMessageId(request.clientMessageId());
        String content = request.content().trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Content is empty");
        }
        message.setContent(content);

        try {
            Message saved = messageRepository.save(message);
            MessageResponse payload = new MessageResponse(
                    saved.getId(),
                    saved.getRoomId(),
                    saved.getSenderId(),
                    saved.getClientMessageId(),
                    saved.getContent(),
                    saved.getCreatedAt()
            );
            presenceService.touchRoomMember(request.roomId(), ws.userId());
            broadcastService.publish(payload);
        } catch (DataIntegrityViolationException ex) {
            // Duplicate clientMessageId for this sender; ignore.
        }
    }
}
