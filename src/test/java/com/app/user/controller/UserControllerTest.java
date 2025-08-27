package com.app.user.controller;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Software Developer")
                .location("San Francisco, CA")
                .website("https://testuser.dev")
                .joinedAt(Instant.now())
                .isVerified(true)
                .followersCount(150)
                .followingCount(200)
                .postsCount(50)
                .totalSpent(2500.00)
                .avgRating(4.5)
                .isOnline(true)
                .build();
    }

    @Test
    void getUserProfile_ValidUsername_ReturnsUserSummary() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        ResponseEntity<UserSummary> response = userController.getUserProfile("testuser");

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        UserSummary userSummary = response.getBody();
        assertThat(userSummary.getId()).isEqualTo(userId);
        assertThat(userSummary.getUsername()).isEqualTo("testuser");
        assertThat(userSummary.getName()).isEqualTo("Test User");
        assertThat(userSummary.getAvatar()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(userSummary.getBio()).isEqualTo("Software Developer");
        assertThat(userSummary.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(userSummary.getWebsite()).isEqualTo("https://testuser.dev");
        assertThat(userSummary.getIsVerified()).isTrue();
        assertThat(userSummary.getFollowersCount()).isEqualTo(150);
        assertThat(userSummary.getFollowingCount()).isEqualTo(200);
        assertThat(userSummary.getPostsCount()).isEqualTo(50);
        assertThat(userSummary.getTotalSpent()).isEqualTo(2500.00);
        assertThat(userSummary.getAvgRating()).isEqualTo(4.5);
        assertThat(userSummary.getIsOnline()).isTrue();
        assertThat(userSummary.getJoinedAt()).isNotNull();
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsRuntimeException() {
        // Given
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userController.getUserProfile(nonExistentUsername))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUsername(nonExistentUsername);
    }

    @Test
    void getUserProfile_UserWithNullFields_HandlesProperly() {
        // Given
        User userWithNulls = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .avatarUrl(null)
                .bio(null)
                .location(null)
                .website(null)
                .joinedAt(Instant.now())
                .isVerified(null)
                .followersCount(null)
                .followingCount(null)
                .postsCount(null)
                .totalSpent(null)
                .avgRating(null)
                .isOnline(null)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userWithNulls));

        // When
        ResponseEntity<UserSummary> response = userController.getUserProfile("testuser");

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        UserSummary userSummary = response.getBody();
        assertThat(userSummary.getId()).isEqualTo(userId);
        assertThat(userSummary.getUsername()).isEqualTo("testuser");
        assertThat(userSummary.getName()).isEqualTo("Test User");
        assertThat(userSummary.getAvatar()).isNull();
        assertThat(userSummary.getBio()).isNull();
        assertThat(userSummary.getLocation()).isNull();
        assertThat(userSummary.getWebsite()).isNull();
        assertThat(userSummary.getIsVerified()).isNull();
        assertThat(userSummary.getFollowersCount()).isNull();
        assertThat(userSummary.getFollowingCount()).isNull();
        assertThat(userSummary.getPostsCount()).isNull();
        assertThat(userSummary.getTotalSpent()).isNull();
        assertThat(userSummary.getAvgRating()).isNull();
        assertThat(userSummary.getIsOnline()).isNull();
    }

    @Test
    void getUserProfile_NewUser_ReturnsBasicProfile() {
        // Given
        User newUser = User.builder()
                .id(userId)
                .username("newuser")
                .displayName("New User")
                .email("new@example.com")
                .joinedAt(Instant.now())
                .isVerified(false)
                .followersCount(0)
                .followingCount(0)
                .postsCount(0)
                .totalSpent(0.0)
                .avgRating(0.0)
                .isOnline(false)
                .build();

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(newUser));

        // When
        ResponseEntity<UserSummary> response = userController.getUserProfile("newuser");

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        UserSummary userSummary = response.getBody();
        assertThat(userSummary.getUsername()).isEqualTo("newuser");
        assertThat(userSummary.getIsVerified()).isFalse();
        assertThat(userSummary.getFollowersCount()).isEqualTo(0);
        assertThat(userSummary.getFollowingCount()).isEqualTo(0);
        assertThat(userSummary.getPostsCount()).isEqualTo(0);
        assertThat(userSummary.getTotalSpent()).isEqualTo(0.0);
        assertThat(userSummary.getAvgRating()).isEqualTo(0.0);
        assertThat(userSummary.getIsOnline()).isFalse();
    }

    @Test
    void followUser_ValidUserId_ReturnsFollowResult() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ResponseEntity<Map<String, Object>> response = userController.followUser(userId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> result = response.getBody();
        assertThat(result.get("following")).isEqualTo(true);
        assertThat(result.get("followersCount")).isEqualTo(151); // Original 150 + 1

        // Verify the user's followers count was incremented
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFollowersCount()).isEqualTo(151);
    }

    @Test
    void followUser_UserWithNullFollowersCount_InitializesToOne() {
        // Given
        User userWithNullFollowers = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .followersCount(null) // Null followers count
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithNullFollowers));
        when(userRepository.save(any(User.class))).thenReturn(userWithNullFollowers);

        // When
        ResponseEntity<Map<String, Object>> response = userController.followUser(userId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> result = response.getBody();
        assertThat(result.get("following")).isEqualTo(true);
        assertThat(result.get("followersCount")).isEqualTo(1);

        // Verify the user's followers count was set to 1
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFollowersCount()).isEqualTo(1);
    }

    @Test
    void followUser_UserWithZeroFollowers_IncrementsToOne() {
        // Given
        User userWithZeroFollowers = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .followersCount(0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithZeroFollowers));
        when(userRepository.save(any(User.class))).thenReturn(userWithZeroFollowers);

        // When
        ResponseEntity<Map<String, Object>> response = userController.followUser(userId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> result = response.getBody();
        assertThat(result.get("following")).isEqualTo(true);
        assertThat(result.get("followersCount")).isEqualTo(1);

        // Verify the user's followers count was incremented
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFollowersCount()).isEqualTo(1);
    }

    @Test
    void followUser_UserNotFound_ThrowsRuntimeException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userController.followUser(nonExistentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(nonExistentUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void followUser_PopularUser_HandlesLargeFollowersCount() {
        // Given
        User popularUser = User.builder()
                .id(userId)
                .username("popularuser")
                .displayName("Popular User")
                .email("popular@example.com")
                .followersCount(999999)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(popularUser));
        when(userRepository.save(any(User.class))).thenReturn(popularUser);

        // When
        ResponseEntity<Map<String, Object>> response = userController.followUser(userId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> result = response.getBody();
        assertThat(result.get("following")).isEqualTo(true);
        assertThat(result.get("followersCount")).isEqualTo(1000000);

        // Verify the user's followers count was incremented correctly
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFollowersCount()).isEqualTo(1000000);
    }

    @Test
    void getUserProfile_UsernameWithSpecialCharacters_HandlesCorrectly() {
        // Given
        String specialUsername = "user.name_123";
        User userWithSpecialName = User.builder()
                .id(userId)
                .username(specialUsername)
                .displayName("Special User")
                .email("special@example.com")
                .joinedAt(Instant.now())
                .build();

        when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(userWithSpecialName));

        // When
        ResponseEntity<UserSummary> response = userController.getUserProfile(specialUsername);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo(specialUsername);
        assertThat(response.getBody().getName()).isEqualTo("Special User");
    }
}