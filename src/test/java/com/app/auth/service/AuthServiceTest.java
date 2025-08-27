package com.app.auth.service;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.security.JwtService;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.UnauthorizedException;
import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .password("encoded-password")
                .avatarUrl("avatar-url")
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
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getName()).isEqualTo("Test User");
        assertThat(response.getUser().getAvatar()).isEqualTo("avatar-url");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_UserNotFound_ThrowsUnauthorizedException() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_ValidData_ReturnsAuthResponse() {
        // Given
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getName()).isEqualTo("Test User");

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBadRequestException() {
        // Given
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already registered");

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsBadRequestException() {
        // Given
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username already taken");

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void getCurrentUser_ValidToken_ReturnsUserSummary() {
        // Given
        String token = "valid-token";
        String username = "testuser";
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserSummary result = authService.getCurrentUser(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getAvatar()).isEqualTo("avatar-url");

        verify(jwtService).extractUsername(token);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsUnauthorizedException() {
        // Given
        String token = "valid-token";
        String username = "nonexistent";
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.getCurrentUser(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User not found");

        verify(jwtService).extractUsername(token);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void register_CreatesUserWithCorrectData() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Capture the user being saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify the user has correct data
            assertThat(savedUser.getUsername()).isEqualTo(registerRequest.getUsername());
            assertThat(savedUser.getDisplayName()).isEqualTo(registerRequest.getName());
            assertThat(savedUser.getEmail()).isEqualTo(registerRequest.getEmail());
            assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
            assertThat(savedUser.getId()).isNotNull();
            return savedUser;
        });

        // When
        authService.register(registerRequest);

        // Then
        verify(userRepository).save(any(User.class));
    }
}