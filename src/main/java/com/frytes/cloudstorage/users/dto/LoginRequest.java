package com.frytes.cloudstorage.users.dto;

public record LoginRequest(
        String username,
        String password
) {
}
