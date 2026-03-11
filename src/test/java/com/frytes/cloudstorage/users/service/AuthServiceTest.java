package com.frytes.cloudstorage.users.service;

import com.frytes.cloudstorage.common.exception.UserAlreadyExistsException;
import com.frytes.cloudstorage.users.dto.RegisterRequest;
import com.frytes.cloudstorage.users.dto.UserMapper;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import com.frytes.cloudstorage.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest("newuser", "pass123");
        User userEntity = new User();
        userEntity.setUsername("newuser");
        userEntity.setRole(Role.USER);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");

        authService.register(request);

        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Ошибка регистрации: пользователь уже существует")
    void shouldThrowExceptionIfUserExists() {
        RegisterRequest request = new RegisterRequest("exists", "pass");
        when(userRepository.existsByUsername("exists")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}