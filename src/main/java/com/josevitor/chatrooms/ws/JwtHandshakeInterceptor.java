package com.josevitor.chatrooms.ws;

import com.josevitor.chatrooms.auth.JwtPrincipal;
import com.josevitor.chatrooms.auth.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (token == null) {
            return false;
        }

        try {
            JwtPrincipal principal = jwtService.parse(token);
            attributes.put("jwt", principal);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String resolveToken(ServerHttpRequest request) {
        List<String> auth = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isEmpty()) {
            String header = auth.get(0);
            if (header.startsWith("Bearer ")) {
                return header.substring("Bearer ".length()).trim();
            }
        }

        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals("token")) {
                return kv[1];
            }
        }
        return null;
    }
}
