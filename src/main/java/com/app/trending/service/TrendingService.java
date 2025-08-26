package com.app.trending.service;

import com.app.post.repo.PostRepository;
import com.app.trending.dto.Stats;
import com.app.trending.dto.TrendingItem;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TrendingService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    public List<TrendingItem> getTrendingItems() {
        // For demo purposes, generate some trending items
        // In a real application, this would be based on actual purchase data
        return Arrays.asList(
            TrendingItem.builder()
                    .id(UUID.randomUUID())
                    .name("iPhone 15 Pro")
                    .icon("📱")
                    .category("Electronics")
                    .purchasesToday(1250)
                    .rank(1)
                    .isHot(true)
                    .isRising(true)
                    .build(),
            TrendingItem.builder()
                    .id(UUID.randomUUID())
                    .name("Air Jordan 1")
                    .icon("👟")
                    .category("Fashion")
                    .purchasesToday(890)
                    .rank(2)
                    .isHot(false)
                    .isRising(true)
                    .build(),
            TrendingItem.builder()
                    .id(UUID.randomUUID())
                    .name("PlayStation 5")
                    .icon("🎮")
                    .category("Gaming")
                    .purchasesToday(756)
                    .rank(3)
                    .isHot(true)
                    .isRising(false)
                    .build(),
            TrendingItem.builder()
                    .id(UUID.randomUUID())
                    .name("MacBook Pro")
                    .icon("💻")
                    .category("Computers")
                    .purchasesToday(623)
                    .rank(4)
                    .isHot(false)
                    .isRising(true)
                    .build(),
            TrendingItem.builder()
                    .id(UUID.randomUUID())
                    .name("AirPods Pro")
                    .icon("🎧")
                    .category("Audio")
                    .purchasesToday(545)
                    .rank(5)
                    .isHot(false)
                    .isRising(false)
                    .build()
        );
    }
    
    public Stats getStats() {
        // Calculate actual stats from the database
        long totalPosts = postRepository.count();
        long totalUsers = userRepository.count();
        
        // For demo purposes, generate some random stats
        // In a real application, this would be calculated from actual data
        return Stats.builder()
                .totalPosts((int) totalPosts)
                .totalMoneySpent(ThreadLocalRandom.current().nextDouble(50000, 200000))
                .activeUsers((int) (totalUsers * 0.7)) // 70% of users are active
                .build();
    }
}