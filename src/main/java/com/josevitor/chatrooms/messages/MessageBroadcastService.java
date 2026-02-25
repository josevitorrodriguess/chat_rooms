package com.josevitor.chatrooms.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.josevitor.chatrooms.config.RedisConfig;
import com.josevitor.chatrooms.messages.dto.MessageResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(MessageBroadcastService.class);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 100;

    public MessageBroadcastService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(MessageResponse message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            boolean published = false;
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                try {
                    redisTemplate.convertAndSend(RedisConfig.MESSAGE_CHANNEL, payload);
                    published = true;
                    break;
                } catch (Exception ex) {
                    log.warn("Redis publish failed (attempt {}): {}", attempt, ex.getMessage());
                    sleepBackoff(attempt);
                }
            }
            if (!published) {
                log.error("Redis publish failed after retries, sending to DLQ");
                redisTemplate.opsForList().leftPush(RedisConfig.MESSAGE_DLQ, payload);
            }
        } catch (Exception ex) {
            log.error("Failed to publish message", ex);
        }
    }

    private void sleepBackoff(int attempt) {
        try {
            long delay = BASE_DELAY_MS * (1L << (attempt - 1));
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
