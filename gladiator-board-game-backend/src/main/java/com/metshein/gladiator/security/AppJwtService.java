package com.metshein.gladiator.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.metshein.gladiator.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AppJwtService {
    
    private final SecretKey signingKey;
    private final long accessTokenTtlSeconds;
    private final String issuer;
    private final String audience;

    public AppJwtService(
        @Value("${auth.jwt.secret}") String jwtSecret,
        @Value("${auth.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds,
        @Value("${auth.jwt.issuer:gladiator-backend}") String issuer,
        @Value("${auth.jwt.audience:gladiator-frontend}") String audience
    ) {
        Objects.requireNonNull(jwtSecret, "auth.jwt.secret is required");
        if (jwtSecret.length() < 32) {
            throw new IllegalArgumentException("auth.jwt.secret must be at least 32 characters long");
        }

        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
            .subject(user.getId().toString())
            .issuer(issuer)
            .audience().add(audience).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("email", user.getEmail())
            .claim("name", user.getName())
            .claim("provider", user.getProvider())
            .claim("roles", List.of("ROLE_USER")) // Add roles as needed
            .id(UUID.randomUUID().toString())
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
