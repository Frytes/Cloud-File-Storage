package com.frytes.cloudstorage.users.dto.request;

import com.frytes.cloudstorage.common.validate.ValidPassword;
import com.frytes.cloudstorage.common.validate.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterRequest(
        @Schema(description = "Желаемое имя пользователя", example = "frytes_dev")
        @ValidUsername String username,

        @Schema(description = "Надежный пароль", example = "SecretPass123!")
        @ValidPassword String password
) {}