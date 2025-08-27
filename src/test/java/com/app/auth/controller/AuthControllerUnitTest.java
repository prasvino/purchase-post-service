package com.app.auth.controller;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.service.AuthService;
import com.app.user.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;
    private UserSummary userSummary;

    @BeforeEach
    void setUp() {
        // Setup test data
        userSummary = UserSummary.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .name("Test User")
                .avatar("avatar-url")
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .user(userSummary)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void login_ValidCredentials_ReturnsAuthResponse() {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void register_ValidData_ReturnsAuthResponse() {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCurrentUser_ValidToken_ReturnsUserSummary() {
        // Given
        String authHeader = "Bearer jwt-token";
        when(authService.getCurrentUser(eq("jwt-token"))).thenReturn(userSummary);

        // When
        ResponseEntity<UserSummary> response = authController.getCurrentUser(authHeader);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getName()).isEqualTo("Test User");
    }

    @Test
    void getCurrentUser_ExtractsTokenCorrectly() {
        // Given
        String authHeader = "Bearer some-jwt-token-value";
        when(authService.getCurrentUser(eq("some-jwt-token-value"))).thenReturn(userSummary);

        // When
        ResponseEntity<UserSummary> response = authController.getCurrentUser(authHeader);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }
}