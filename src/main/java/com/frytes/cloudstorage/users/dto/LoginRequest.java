package com.frytes.cloudstorage.users.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Логин не может быть пустым")
        String username,

        @NotBlank(message = "Пароль не может быть пустым")
        String password
) {}
