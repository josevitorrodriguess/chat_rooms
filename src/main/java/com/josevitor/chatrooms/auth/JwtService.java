package com.josevitor.chatrooms.auth;

import com.josevitor.chatrooms.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.expirationMinutes() * 60);

        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("un", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = claims.get("uid", Long.class);
        String username = claims.get("un", String.class);
        return new JwtPrincipal(userId, username);
    }
}
