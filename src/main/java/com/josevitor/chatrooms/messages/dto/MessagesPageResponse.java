package com.josevitor.chatrooms.messages.dto;

import java.util.List;

public record MessagesPageResponse(
        List<MessageResponse> items,
        String nextCursor
) {
}
