package com.metshein.gladiator.security;

public record GoogleIdentity(String subject, String email, String name, String picture) {
    
}
