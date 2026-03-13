package com.frytes.cloudstorage.users.dto.response;

import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

@Schema(description = "Ответ сервера с данными профиля пользователя")
public record AuthResponse(
        @Schema(description = "Имя пользователя", example = "frytes_dev")
        String username,

        @Schema(description = "Роль пользователя в системе", example = "USER")
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