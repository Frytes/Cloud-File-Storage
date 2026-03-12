package com.frytes.cloudstorage.users.dto;

import com.frytes.cloudstorage.users.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;

public record AuthResponse(
        String username,
        String role
) {
    public static AuthResponse from(CustomUserDetails user) {
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .orElse("USER");

        return new AuthResponse(user.getUsername(), role);
    }
}
