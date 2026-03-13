package com.frytes.cloudstorage.users.controller;

import com.frytes.cloudstorage.users.api.UserApi;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    @GetMapping("/me")
    @Override
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(AuthResponse.from(user));
    }
}