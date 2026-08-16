package com.metshein.gladiator.security;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifierService {
    
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

    private final JwtDecoder googleJwtDecoder;
    private final String googleClientId;

    public GoogleTokenVerifierService(@Value("${auth.google.client-id}") String googleClientId) {
        this.googleClientId = Objects.requireNonNull(googleClientId, "auth.google.client-id is required");
        this.googleJwtDecoder = JwtDecoders.fromIssuerLocation(GOOGLE_ISSUER);
    }

    public GoogleIdentity verify(String idToken) {
        try {
            Jwt jwt = googleJwtDecoder.decode(idToken);
            validateAudience(jwt);

            Boolean emailVerified = jwt.getClaim("email_verified");
            if (!Boolean.TRUE.equals(emailVerified)) {
                throw new BadCredentialsException("Google account email is not verified");
            }

            String subject = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            String picture = jwt.getClaimAsString("picture");

            if (subject == null || email == null || email.isBlank()) {
                throw new BadCredentialsException("Google token missing required claims");
            }

            return new GoogleIdentity(subject, email.toLowerCase(), name, picture);
        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid Google ID token", ex);
        }
    }

    private void validateAudience(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience == null || audience.stream().noneMatch(googleClientId::equals)) {
            throw new BadCredentialsException("Google token audience mismatch");
        }
    }
}
