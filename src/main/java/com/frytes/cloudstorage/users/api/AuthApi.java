package com.frytes.cloudstorage.users.api;

import com.frytes.cloudstorage.common.dto.ErrorResponse;
import com.frytes.cloudstorage.users.dto.request.LoginRequest;
import com.frytes.cloudstorage.users.dto.request.RegisterRequest;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Авторизация", description = "API для регистрации, входа и выхода пользователей")
public interface AuthApi {

    @Operation(summary = "Регистрация нового пользователя", description = "Создает новый аккаунт и автоматически авторизует пользователя (создает сессию)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации полей (например, слишком короткий пароль)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя по логину и паролю, возвращает cookie с ID сессии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(summary = "Выход из системы", description = "Инвалидирует текущую сессию пользователя и очищает SecurityContext")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешный выход (без тела ответа)"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void logout(HttpServletRequest httpRequest);
}