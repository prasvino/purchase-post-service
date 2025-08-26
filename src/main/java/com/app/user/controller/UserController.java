package com.app.user.controller;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{username}")
    public ResponseEntity<UserSummary> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .name(user.getDisplayName())
                .username(user.getUsername())
                .avatar(user.getAvatarUrl())
                .bio(user.getBio())
                .location(user.getLocation())
                .website(user.getWebsite())
                .joinedAt(user.getJoinedAt())
                .isVerified(user.getIsVerified())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .postsCount(user.getPostsCount())
                .totalSpent(user.getTotalSpent())
                .avgRating(user.getAvgRating())
                .isOnline(user.getIsOnline())
                .build();
        
        return ResponseEntity.ok(userSummary);
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Object>> followUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Increment followers count
        user.setFollowersCount(user.getFollowersCount() != null ? user.getFollowersCount() + 1 : 1);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of(
            "following", true,
            "followersCount", user.getFollowersCount()
        ));
    }
}