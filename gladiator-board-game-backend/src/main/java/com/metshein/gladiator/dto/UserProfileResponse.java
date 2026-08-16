package com.metshein.gladiator.dto;

import java.util.UUID;

import com.metshein.gladiator.entity.User;

public record UserProfileResponse(
    UUID id,
    String email,
    String name
) {
    // Factory method to create a UserProfileResponse from a User entity
    public static UserProfileResponse fromUser(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}