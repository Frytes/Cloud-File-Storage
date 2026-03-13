package com.frytes.cloudstorage.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Стандартный формат ответа при возникновении ошибки")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        @Schema(description = "HTTP статус код", example = "400")
        int status,

        @Schema(description = "Тип ошибки", example = "Bad Request")
        String error,

        @Schema(description = "Подробное сообщение об ошибке", example = "Недопустимый формат пути")
        String message,

        @Schema(description = "URI запроса, вызвавшего ошибку", example = "/api/directory")
        String path,

        @Schema(description = "Время возникновения ошибки", example = "2024-03-12T15:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Список ошибок валидации полей (если есть)")
        Map<String, String> validationErrors
) {
    public ErrorResponse {
        validationErrors = validationErrors == null ? null : Map.copyOf(validationErrors);
    }

    public ErrorResponse(int status,
                         String error,
                         String message,
                         String path,
                         LocalDateTime timestamp) {
        this(status, error, message, path, timestamp, null);
    }
}