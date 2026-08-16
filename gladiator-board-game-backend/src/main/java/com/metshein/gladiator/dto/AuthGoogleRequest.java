package com.metshein.gladiator.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthGoogleRequest(@NotBlank(message = "credential is required") String credential) {}
