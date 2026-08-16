package com.metshein.gladiator.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserProfileResponse user
) {
}
