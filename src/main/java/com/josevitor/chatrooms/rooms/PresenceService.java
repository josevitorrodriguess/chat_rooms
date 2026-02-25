package com.josevitor.chatrooms.rooms;

import com.josevitor.chatrooms.config.PresenceProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PresenceService {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public PresenceService(StringRedisTemplate redisTemplate, PresenceProperties properties) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(properties.ttlSeconds());
    }

    public void touchRoomMember(Long roomId, Long userId) {
        String key = presenceKey(roomId, userId);
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    public void removeRoomMember(Long roomId, Long userId) {
        String key = presenceKey(roomId, userId);
        redisTemplate.delete(key);
    }

    private String presenceKey(Long roomId, Long userId) {
        return "presence:room:" + roomId + ":user:" + userId;
    }
}
