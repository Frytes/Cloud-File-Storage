package com.frytes.cloudstorage.users.service;

import com.frytes.cloudstorage.common.exception.UserAlreadyExistsException;
import com.frytes.cloudstorage.users.dto.AuthResponse;
import com.frytes.cloudstorage.users.dto.LoginRequest;
import com.frytes.cloudstorage.users.dto.RegisterRequest;
import com.frytes.cloudstorage.users.dto.UserMapper;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import com.frytes.cloudstorage.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpSession httpSession;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация и автоматический вход")
    void shouldRegisterAndLoginUser() {
        RegisterRequest request = new RegisterRequest("newuser", "pass123");
        User userEntity = new User();
        userEntity.setUsername("newuser");
        userEntity.setRole(Role.USER);

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(httpSession);
        when(authentication.getName()).thenReturn("newuser");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        AuthResponse response = authService.register(request, httpRequest);

        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo("USER");

        verify(userRepository).save(userEntity);
        verify(authenticationManager).authenticate(any());
        verify(httpSession).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    @DisplayName("Ошибка регистрации: пользователь уже существует")
    void shouldThrowExceptionIfUserExists() {
        RegisterRequest request = new RegisterRequest("exists", "pass");
        when(userRepository.existsByUsername("exists")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, httpRequest))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user", "pass");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(httpSession);
        when(authentication.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        AuthResponse response = authService.login(request, httpRequest);

        assertThat(response.username()).isEqualTo("user");
        assertThat(response.role()).isEqualTo("USER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(httpSession).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    @DisplayName("Ошибка входа: неверные данные")
    void shouldThrowExceptionOnBadCredentials() {
        LoginRequest request = new LoginRequest("user", "wrong_pass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad creds"));

        assertThatThrownBy(() -> authService.login(request, httpRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(httpRequest, never()).getSession(true);
    }
}