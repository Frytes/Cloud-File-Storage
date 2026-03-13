package com.frytes.cloudstorage.users.api;

import com.frytes.cloudstorage.common.dto.ErrorResponse;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Пользователи", description = "API для получения информации о текущем пользователе")
public interface UserApi {

    @Operation(summary = "Получить профиль пользователя", description = "Возвращает данные авторизованного пользователя на основе текущей сессии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован или сессия истекла", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AuthResponse> getCurrentUser(
            @Parameter(hidden = true) CustomUserDetails user
    );
}