package com.metshein.gladiator.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public record AppUserPrincipal(
    String userId,
    String email,
    Collection<? extends GrantedAuthority> authorities
) {
}
