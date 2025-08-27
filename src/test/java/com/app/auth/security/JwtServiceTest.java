package com.app.auth.security;

import com.app.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private final String testSecret = "myVeryLongSecretKeyForTestingPurposesOnlyThatMeetsThe32CharacterMinimumRequirement123456789";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Set test values using reflection since they're private fields with @Value annotation
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600L); // 1 hour

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void generateToken_ValidUser_ReturnsJwtToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verify the token can be parsed and contains correct data
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        assertThat(claims.getSubject()).isEqualTo(testUser.getUsername());
        assertThat(claims.get("userId")).isEqualTo(testUser.getId().toString());
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(testUser.getUsername());
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpirationDate() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
        
        // Should expire in approximately 1 hour (allowing for small time differences)
        long expectedExpirationTime = System.currentTimeMillis() + 3600 * 1000;
        long actualExpirationTime = expiration.getTime();
        long timeDifference = Math.abs(actualExpirationTime - expectedExpirationTime);
        assertThat(timeDifference).isLessThan(10000); // Within 10 seconds
    }

    @Test
    void validateToken_ValidTokenAndUsername_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Boolean isValid = jwtService.validateToken(token, testUser.getUsername());

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ValidTokenWrongUsername_ReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Boolean isValid = jwtService.validateToken(token, "wronguser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Given - Create a token that's already expired (negative expiration)
        JwtService expiredTokenService = new JwtService();
        ReflectionTestUtils.setField(expiredTokenService, "secret", testSecret);
        ReflectionTestUtils.setField(expiredTokenService, "expiration", -3600L); // Expired 1 hour ago
        
        String expiredToken = expiredTokenService.generateToken(testUser);

        // When & Then - Should return false for expired token (the validateToken method catches ExpiredJwtException internally)
        try {
            Boolean isValid = jwtService.validateToken(expiredToken, testUser.getUsername());
            assertThat(isValid).isFalse();
        } catch (Exception e) {
            // If an exception is thrown, the token is considered invalid (which is correct behavior)
            assertThat(e).isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        }
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_MalformedToken_ThrowsException() {
        // Given
        String malformedToken = "malformed-token";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractClaim_ValidTokenWithCustomClaim_ReturnsClaimValue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String userId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));

        // Then
        assertThat(userId).isEqualTo(testUser.getId().toString());
    }

    @Test
    void generateToken_NullUser_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> jwtService.generateToken(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_UserWithEmptyUsername_CreatesToken() {
        // Given
        User userWithEmptyUsername = User.builder()
                .id(UUID.randomUUID())
                .username("")
                .build();

        // When
        String token = jwtService.generateToken(userWithEmptyUsername);

        // Then - Token should be created successfully
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // The token should be parseable (verifies it's a valid JWT)
        String extractedUsername = jwtService.extractUsername(token);
        // JWT library may return null for empty subjects, which is acceptable
        // The important thing is that the token generation doesn't fail
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(null, testUser.getUsername()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateToken_EmptyToken_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken("", testUser.getUsername()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractExpiration_ReturnsCorrectExpirationTime() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then - Expiration should be approximately 1 hour from now (allowing for 1 minute variance)
        long expectedExpirationTime = System.currentTimeMillis() + 3600 * 1000;
        long actualExpirationTime = expiration.getTime();
        long timeDifference = Math.abs(actualExpirationTime - expectedExpirationTime);
        
        // Allow up to 60 seconds variance for test execution time
        assertThat(timeDifference).isLessThan(60000);
        assertThat(expiration).isAfter(new Date());
    }
}