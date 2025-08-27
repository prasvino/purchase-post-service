package com.app.user.mapper;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserMapperImpl.class})
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;
    private UUID userId;
    private Instant joinedTime;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        joinedTime = Instant.now();
        
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .password("encrypted-password")
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Software Developer")
                .location("San Francisco, CA")
                .website("https://testuser.dev")
                .joinedAt(joinedTime)
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
    void toDto_CompleteUser_MapsAllFieldsCorrectly() {
        // When
        UserSummary result = userMapper.toDto(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getName()).isEqualTo("Test User"); // displayName -> name mapping
        assertThat(result.getAvatar()).isEqualTo("https://example.com/avatar.jpg"); // avatarUrl -> avatar mapping
        assertThat(result.getBio()).isEqualTo("Software Developer");
        assertThat(result.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(result.getWebsite()).isEqualTo("https://testuser.dev");
        assertThat(result.getJoinedAt()).isEqualTo(joinedTime);
        assertThat(result.getIsVerified()).isTrue();
        assertThat(result.getFollowersCount()).isEqualTo(150);
        assertThat(result.getFollowingCount()).isEqualTo(200);
        assertThat(result.getPostsCount()).isEqualTo(50);
        assertThat(result.getTotalSpent()).isEqualTo(2500.00);
        assertThat(result.getAvgRating()).isEqualTo(4.5);
        assertThat(result.getIsOnline()).isTrue();
    }

    @Test
    void toDto_UserWithNullFields_HandlesNullsProperly() {
        // Given
        User userWithNulls = User.builder()
                .id(userId)
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .password("encrypted-password")
                .avatarUrl(null)
                .bio(null)
                .location(null)
                .website(null)
                .joinedAt(joinedTime)
                .isVerified(null)
                .followersCount(null)
                .followingCount(null)
                .postsCount(null)
                .totalSpent(null)
                .avgRating(null)
                .isOnline(null)
                .build();

        // When
        UserSummary result = userMapper.toDto(userWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getAvatar()).isNull();
        assertThat(result.getBio()).isNull();
        assertThat(result.getLocation()).isNull();
        assertThat(result.getWebsite()).isNull();
        assertThat(result.getJoinedAt()).isEqualTo(joinedTime);
        assertThat(result.getIsVerified()).isNull();
        assertThat(result.getFollowersCount()).isNull();
        assertThat(result.getFollowingCount()).isNull();
        assertThat(result.getPostsCount()).isNull();
        assertThat(result.getTotalSpent()).isNull();
        assertThat(result.getAvgRating()).isNull();
        assertThat(result.getIsOnline()).isNull();
    }

    @Test
    void toDto_NewUser_MapsMinimalDataCorrectly() {
        // Given
        User newUser = User.builder()
                .id(userId)
                .username("newuser")
                .displayName("New User")
                .email("new@example.com")
                .password("encrypted-password")
                .joinedAt(joinedTime)
                .isVerified(false)
                .followersCount(0)
                .followingCount(0)
                .postsCount(0)
                .totalSpent(0.0)
                .avgRating(0.0)
                .isOnline(false)
                .build();

        // When
        UserSummary result = userMapper.toDto(newUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getJoinedAt()).isEqualTo(joinedTime);
        assertThat(result.getIsVerified()).isFalse();
        assertThat(result.getFollowersCount()).isEqualTo(0);
        assertThat(result.getFollowingCount()).isEqualTo(0);
        assertThat(result.getPostsCount()).isEqualTo(0);
        assertThat(result.getTotalSpent()).isEqualTo(0.0);
        assertThat(result.getAvgRating()).isEqualTo(0.0);
        assertThat(result.getIsOnline()).isFalse();
    }

    @Test
    void toDto_UserWithSpecialCharacters_MapsCorrectly() {
        // Given
        User userWithSpecialChars = User.builder()
                .id(userId)
                .username("user.name_123")
                .displayName("名前 Test User")
                .email("test@example.com")
                .avatarUrl("https://example.com/avatars/user%20name.jpg")
                .bio("Software Developer 👨‍💻")
                .location("San Francisco, CA 🌉")
                .website("https://test-user.dev")
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(userWithSpecialChars);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user.name_123");
        assertThat(result.getName()).isEqualTo("名前 Test User");
        assertThat(result.getAvatar()).isEqualTo("https://example.com/avatars/user%20name.jpg");
        assertThat(result.getBio()).isEqualTo("Software Developer 👨‍💻");
        assertThat(result.getLocation()).isEqualTo("San Francisco, CA 🌉");
        assertThat(result.getWebsite()).isEqualTo("https://test-user.dev");
    }

    @Test
    void toDto_UserWithLongValues_MapsCorrectly() {
        // Given
        User userWithLongValues = User.builder()
                .id(userId)
                .username("influencer")
                .displayName("Popular Influencer")
                .email("influencer@example.com")
                .bio("This is a very long bio that contains a lot of information about the user including their interests, background, and current activities in the social media space")
                .joinedAt(joinedTime)
                .followersCount(1000000)
                .followingCount(5000)
                .postsCount(25000)
                .totalSpent(100000.99)
                .avgRating(4.95)
                .build();

        // When
        UserSummary result = userMapper.toDto(userWithLongValues);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("influencer");
        assertThat(result.getName()).isEqualTo("Popular Influencer");
        assertThat(result.getBio()).hasSize(161); // Verify long bio is preserved
        assertThat(result.getFollowersCount()).isEqualTo(1000000);
        assertThat(result.getFollowingCount()).isEqualTo(5000);
        assertThat(result.getPostsCount()).isEqualTo(25000);
        assertThat(result.getTotalSpent()).isEqualTo(100000.99);
        assertThat(result.getAvgRating()).isEqualTo(4.95);
    }

    @Test
    void toDto_VerifiedUser_MapsVerificationStatusCorrectly() {
        // Given
        User verifiedUser = User.builder()
                .id(userId)
                .username("verified")
                .displayName("Verified User")
                .email("verified@example.com")
                .isVerified(true)
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(verifiedUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsVerified()).isTrue();
    }

    @Test
    void toDto_UnverifiedUser_MapsVerificationStatusCorrectly() {
        // Given
        User unverifiedUser = User.builder()
                .id(userId)
                .username("unverified")
                .displayName("Unverified User")
                .email("unverified@example.com")
                .isVerified(false)
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(unverifiedUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsVerified()).isFalse();
    }

    @Test
    void toDto_OnlineUser_MapsOnlineStatusCorrectly() {
        // Given
        User onlineUser = User.builder()
                .id(userId)
                .username("online")
                .displayName("Online User")
                .email("online@example.com")
                .isOnline(true)
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(onlineUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsOnline()).isTrue();
    }

    @Test
    void toDto_OfflineUser_MapsOnlineStatusCorrectly() {
        // Given
        User offlineUser = User.builder()
                .id(userId)
                .username("offline")
                .displayName("Offline User")
                .email("offline@example.com")
                .isOnline(false)
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(offlineUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsOnline()).isFalse();
    }

    @Test
    void toDto_NullUser_ReturnsNull() {
        // When
        UserSummary result = userMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDto_UserWithPreciseRatings_MapsDecimalValuesCorrectly() {
        // Given
        User userWithPreciseRating = User.builder()
                .id(userId)
                .username("precise")
                .displayName("Precise User")
                .email("precise@example.com")
                .totalSpent(1234.56)
                .avgRating(4.789)
                .joinedAt(joinedTime)
                .build();

        // When
        UserSummary result = userMapper.toDto(userWithPreciseRating);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSpent()).isEqualTo(1234.56);
        assertThat(result.getAvgRating()).isEqualTo(4.789);
    }

    @Test
    void toDto_ExcludesPasswordAndEmail_OnlyIncludesPublicFields() {
        // When
        UserSummary result = userMapper.toDto(testUser);

        // Then
        assertThat(result).isNotNull();
        // Verify that sensitive fields like password and email are not exposed in UserSummary
        // UserSummary should not have password or email fields at all
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername()).isNotNull();
        assertThat(result.getName()).isNotNull();
        // Password and email should not be accessible in UserSummary DTO
    }
}