package com.app.auth.service;

import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.common.exception.BadRequestException;
import com.app.common.exception.UnauthorizedException;
import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.app.auth.security.JwtService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user);
        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getDisplayName())
                .avatar(user.getAvatarUrl())
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .user(userSummary)
                .build();
    }
    
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }
        
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already taken");
        }
        
        // Create new user
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .displayName(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatarUrl(null)
                .build();
        
        user = userRepository.save(user);
        
        String token = jwtService.generateToken(user);
        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getDisplayName())
                .avatar(user.getAvatarUrl())
                .build();
        
        return AuthResponse.builder()
                .token(token)
                .user(userSummary)
                .build();
    }
    
    public UserSummary getCurrentUser(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getDisplayName())
                .avatar(user.getAvatarUrl())
                .build();
    }
}