package com.josevitor.chatrooms.ws;

import com.josevitor.chatrooms.auth.JwtPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object raw = attributes.get("jwt");
        if (raw instanceof JwtPrincipal principal) {
            return new WebSocketPrincipal(principal.userId(), principal.username());
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
