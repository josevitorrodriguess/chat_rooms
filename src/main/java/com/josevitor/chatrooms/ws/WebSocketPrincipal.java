package com.josevitor.chatrooms.ws;

import java.security.Principal;

public record WebSocketPrincipal(Long userId, String username) implements Principal {
    @Override
    public String getName() {
        return username;
    }
}
