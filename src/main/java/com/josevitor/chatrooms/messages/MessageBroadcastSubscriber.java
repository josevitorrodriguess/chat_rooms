package com.josevitor.chatrooms.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.josevitor.chatrooms.config.RedisConfig;
import com.josevitor.chatrooms.messages.dto.MessageResponse;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageBroadcastSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public MessageBroadcastSubscriber(SimpMessagingTemplate messagingTemplate,
                                      RedisMessageListenerContainer container,
                                      ChannelTopic topic) {
        this.messagingTemplate = messagingTemplate;
        container.addMessageListener(this, topic);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            MessageResponse dto = objectMapper.readValue(payload, MessageResponse.class);
            messagingTemplate.convertAndSend("/topic/rooms/" + dto.roomId(), dto);
        } catch (Exception ignored) {
        }
    }
}
