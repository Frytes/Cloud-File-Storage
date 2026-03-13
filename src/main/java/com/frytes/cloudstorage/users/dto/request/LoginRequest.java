package com.frytes.cloudstorage.users.dto.request;

import com.frytes.cloudstorage.common.validate.ValidPassword;
import com.frytes.cloudstorage.common.validate.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на авторизацию (вход в систему)")
public record LoginRequest(
        @Schema(description = "Уникальное имя пользователя (только латиница и цифры)", example = "frytes_dev")
        @ValidUsername String username,

        @Schema(description = "Пароль пользователя (минимум 8 символов)", example = "SecretPass123!")
        @ValidPassword String password
) {}