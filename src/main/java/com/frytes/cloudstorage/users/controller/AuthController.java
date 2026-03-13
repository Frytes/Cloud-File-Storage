package com.frytes.cloudstorage.users.controller;

import com.frytes.cloudstorage.users.api.AuthApi;
import com.frytes.cloudstorage.users.dto.request.LoginRequest;
import com.frytes.cloudstorage.users.dto.request.RegisterRequest;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import com.frytes.cloudstorage.users.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public AuthResponse register(@RequestBody @Valid RegisterRequest request,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        authService.register(request);
        return authService.authenticateAndCreateSession(request.username(), request.password(), httpRequest, httpResponse);
    }

    @PostMapping("/sign-in")
    @Override
    public AuthResponse login(@RequestBody @Valid LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        return authService.authenticateAndCreateSession(request.username(), request.password(), httpRequest, httpResponse);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void logout(HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        log.info("User '{}' successfully logged out", username);
    }
}