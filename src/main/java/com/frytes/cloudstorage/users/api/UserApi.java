package com.frytes.cloudstorage.users.api;

import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Пользователи", description = "API для получения информации о текущем пользователе")
public interface UserApi {

    @Operation(summary = "Получить профиль пользователя", description = "Возвращает данные авторизованного пользователя на основе текущей сессии")
    ResponseEntity<AuthResponse> getCurrentUser(
            @Parameter(hidden = true) CustomUserDetails user
    );
}