package com.frytes.cloudstorage.users.service;

import com.frytes.cloudstorage.common.exception.UserAlreadyExistsException;
import com.frytes.cloudstorage.users.dto.request.RegisterRequest;
import com.frytes.cloudstorage.users.dto.response.AuthResponse;
import com.frytes.cloudstorage.users.dto.UserMapper;
import com.frytes.cloudstorage.users.model.Provider;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import com.frytes.cloudstorage.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed: Username '{}' already exists", request.username());
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        User user = userMapper.toEntity(request);
        user.setProvider(Provider.LOCAL);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        log.info("Successfully registered new user: {}", user.getUsername());
    }

    public AuthResponse authenticateAndCreateSession(String username, String password,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);

        log.info("User '{}' successfully logged in", authentication.getName());

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        return new AuthResponse(authentication.getName(), role);
    }
}