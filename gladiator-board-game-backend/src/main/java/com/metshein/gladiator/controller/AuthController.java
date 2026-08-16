package com.metshein.gladiator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.metshein.gladiator.dto.AuthGoogleRequest;
import com.metshein.gladiator.dto.AuthResponse;
import com.metshein.gladiator.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid AuthGoogleRequest request) {
        AuthResponse authResponse = authService.loginWithGoogle(request.credential());
        return ResponseEntity.ok(authResponse);
    }
}
