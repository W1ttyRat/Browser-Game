package com.metshein.gladiator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metshein.gladiator.dto.AuthResponse;
import com.metshein.gladiator.dto.UserProfileResponse;
import com.metshein.gladiator.entity.User;
import com.metshein.gladiator.repository.UserRepository;
import com.metshein.gladiator.security.AppJwtService;
import com.metshein.gladiator.security.GoogleIdentity;
import com.metshein.gladiator.security.GoogleTokenVerifierService;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final AppJwtService appJwtService;

    public AuthService(
        UserRepository userRepository,
        GoogleTokenVerifierService googleTokenVerifierService,
        AppJwtService appJwtService
    ) {
        this.userRepository = userRepository;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.appJwtService = appJwtService;
    }

    @Transactional
    public AuthResponse loginWithGoogle(String googleCredential) {
        GoogleIdentity googleIdentity = googleTokenVerifierService.verify(googleCredential);

        User user = userRepository.findByEmail(googleIdentity.email())
            .map(existing -> updateUser(existing, googleIdentity))
            .orElseGet(() -> createUser(googleIdentity));

        String accessToken = appJwtService.createAccessToken(user);

        return new AuthResponse(
            accessToken,
            "Bearer",
            appJwtService.getAccessTokenTtlSeconds(),
            UserProfileResponse.fromUser(user)
        );
    }

    private User updateUser(User user, GoogleIdentity identity) {
        if (identity.name() != null && !identity.name().isBlank()) {
            user.setName(identity.name().trim());
        }
        user.setProvider("google");
        return userRepository.save(user);
    }

    private User createUser(GoogleIdentity identity) {
        User user = new User();
        user.setEmail(identity.email());
        user.setName(identity.name() == null || identity.name().isBlank() ? "User" : identity.name().trim());
        user.setProvider("google");
        return userRepository.save(user);
    }
}
