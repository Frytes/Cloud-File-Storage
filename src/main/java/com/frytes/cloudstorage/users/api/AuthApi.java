package com.frytes.cloudstorage.users.api;

import com.frytes.cloudstorage.users.dto.request.LoginRequest;
import com.frytes.cloudstorage.users.dto.request.RegisterRequest;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Авторизация", description = "API для регистрации, входа и выхода пользователей")
public interface AuthApi {

    @Operation(summary = "Регистрация нового пользователя", description = "Создает новый аккаунт и автоматически авторизует пользователя (создает сессию)")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован")
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя по логину и паролю, возвращает cookie с ID сессии")
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    @Operation(summary = "Выход из системы", description = "Инвалидирует текущую сессию пользователя и очищает SecurityContext")
    @ApiResponse(responseCode = "204", description = "Успешный выход (без тела ответа)")
    void logout(HttpServletRequest httpRequest);
}