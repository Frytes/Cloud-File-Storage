package com.frytes.cloudstorage.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
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