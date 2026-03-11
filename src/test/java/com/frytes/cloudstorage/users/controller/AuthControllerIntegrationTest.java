package com.frytes.cloudstorage.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.users.dto.LoginRequest;
import com.frytes.cloudstorage.users.dto.RegisterRequest;
import com.frytes.cloudstorage.users.model.Provider;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import com.frytes.cloudstorage.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.hamcrest.Matchers.notNullValue;
import static org.testcontainers.shaded.org.hamcrest.Matchers.nullValue;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("test_user")
                .password(passwordEncoder.encode("Secret123!"))
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("Успешный вход: возвращает 200 OK и создает Security сессию")
    void shouldLoginSuccessfullyAndCreateSession() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "Secret123!");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(request().sessionAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, notNullValue()));
        }

    @Test
    @DisplayName("Провал входа: неверный пароль возвращает 401 Unauthorized")
    void shouldFailLoginWithWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("test_user", "WrongPassword!");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(request().sessionAttributeDoesNotExist(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }

    @Test
    @DisplayName("Успешная регистрация: возвращает 201 Created и сразу авторизует")
    void shouldRegisterAndAutoLoginUser() throws Exception {
        RegisterRequest request = new RegisterRequest("new_awesome_user", "SuperSecretPass1!");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new_awesome_user"))
                .andExpect(request().sessionAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, nullValue()));
    }
}