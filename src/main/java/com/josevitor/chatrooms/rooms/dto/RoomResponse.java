package com.josevitor.chatrooms.rooms.dto;

import java.time.Instant;

public record RoomResponse(
        Long id,
        String name,
        String joinCode,
        int maxMembers,
        int currentMembers,
        Long creatorId,
        Instant createdAt,
        Instant expiresAt
) {
}
