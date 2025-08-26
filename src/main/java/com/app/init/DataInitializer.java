package com.app.init;

import com.app.platform.entity.Platform;
import com.app.platform.repo.PlatformRepository;
import com.app.post.entity.Post;
import com.app.post.repo.PostRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create sample users
        if (userRepository.count() == 0) {
            createSampleUsers();
        }

        // Create sample platforms
        if (platformRepository.count() == 0) {
            createSamplePlatforms();
        }

        // Create sample posts
        if (postRepository.count() == 0) {
            createSamplePosts();
        }
    }

    private void createSampleUsers() {
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .displayName("John Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .avatarUrl("https://ui-avatars.com/api/?name=John+Doe&background=random")
                .bio("Tech enthusiast and early adopter")
                .location("San Francisco, CA")
                .website("https://johndoe.com")
                .joinedAt(Instant.now())
                .isVerified(true)
                .followersCount(1250)
                .followingCount(340)
                .postsCount(45)
                .totalSpent(15420.50)
                .avgRating(4.5)
                .isOnline(true)
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("janesmith")
                .displayName("Jane Smith")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .avatarUrl("https://ui-avatars.com/api/?name=Jane+Smith&background=random")
                .bio("Fashion lover and shopping addict")
                .location("New York, NY")
                .website("https://janesmith.com")
                .joinedAt(Instant.now())
                .isVerified(false)
                .followersCount(890)
                .followingCount(210)
                .postsCount(32)
                .totalSpent(8750.25)
                .avgRating(4.2)
                .isOnline(false)
                .build();

        User user3 = User.builder()
                .id(UUID.randomUUID())
                .username("mikejohnson")
                .displayName("Mike Johnson")
                .email("mike@example.com")
                .password(passwordEncoder.encode("password123"))
                .avatarUrl("https://ui-avatars.com/api/?name=Mike+Johnson&background=random")
                .bio("Gamer and tech reviewer")
                .location("Austin, TX")
                .joinedAt(Instant.now())
                .isVerified(true)
                .followersCount(2100)
                .followingCount(150)
                .postsCount(78)
                .totalSpent(22500.00)
                .avgRating(4.8)
                .isOnline(true)
                .build();

        userRepository.saveAll(Arrays.asList(user1, user2, user3));
    }

    private void createSamplePlatforms() {
        Platform platform1 = Platform.builder()
                .id(UUID.randomUUID())
                .name("Amazon")
                .domain("amazon.com")
                .logoUrl("https://amazon.com/favicon.ico")
                .verified(true)
                .build();

        Platform platform2 = Platform.builder()
                .id(UUID.randomUUID())
                .name("eBay")
                .domain("ebay.com")
                .logoUrl("https://ebay.com/favicon.ico")
                .verified(true)
                .build();

        Platform platform3 = Platform.builder()
                .id(UUID.randomUUID())
                .name("Best Buy")
                .domain("bestbuy.com")
                .logoUrl("https://bestbuy.com/favicon.ico")
                .verified(true)
                .build();

        platformRepository.saveAll(Arrays.asList(platform1, platform2, platform3));
    }

    private void createSamplePosts() {
        User user1 = userRepository.findByUsername("johndoe").orElseThrow();
        User user2 = userRepository.findByUsername("janesmith").orElseThrow();
        User user3 = userRepository.findByUsername("mikejohnson").orElseThrow();

        Platform amazon = platformRepository.findByName("Amazon").orElseThrow();
        Platform ebay = platformRepository.findByName("eBay").orElseThrow();
        Platform bestbuy = platformRepository.findByName("Best Buy").orElseThrow();

        Post post1 = Post.builder()
                .author(user1)
                .text("Just got the new iPhone 15 Pro! The camera is absolutely incredible. Best purchase I've made this year!")
                .purchaseDate(LocalDate.now().minusDays(2))
                .price(new BigDecimal("1199.00"))
                .currency("USD")
                .platform(amazon)
                .productUrl("https://amazon.com/iphone-15-pro")
                .visibility("public")
                .likeCount(45)
                .commentCount(12)
                .repostCount(8)
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();

        Post post2 = Post.builder()
                .author(user2)
                .text("Found this amazing vintage leather jacket on eBay! Perfect condition and such a great deal.")
                .purchaseDate(LocalDate.now().minusDays(5))
                .price(new BigDecimal("89.99"))
                .currency("USD")
                .platform(ebay)
                .productUrl("https://ebay.com/vintage-jacket")
                .visibility("public")
                .likeCount(23)
                .commentCount(7)
                .repostCount(3)
                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .build();

        Post post3 = Post.builder()
                .author(user3)
                .text("Upgraded my gaming setup with a new 4K monitor from Best Buy. The difference is night and day!")
                .purchaseDate(LocalDate.now().minusDays(1))
                .price(new BigDecimal("449.99"))
                .currency("USD")
                .platform(bestbuy)
                .productUrl("https://bestbuy.com/4k-monitor")
                .visibility("public")
                .likeCount(67)
                .commentCount(15)
                .repostCount(12)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        Post post4 = Post.builder()
                .author(user1)
                .text("These AirPods Pro are worth every penny. The noise cancellation is perfect for my commute.")
                .purchaseDate(LocalDate.now().minusDays(3))
                .price(new BigDecimal("249.00"))
                .currency("USD")
                .platform(amazon)
                .productUrl("https://amazon.com/airpods-pro")
                .visibility("public")
                .likeCount(34)
                .commentCount(9)
                .repostCount(5)
                .createdAt(Instant.now().minus(3, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(3, ChronoUnit.DAYS))
                .build();

        Post post5 = Post.builder()
                .author(user2)
                .text("Finally got my hands on the PlayStation 5! The loading times are incredibly fast.")
                .purchaseDate(LocalDate.now().minusDays(4))
                .price(new BigDecimal("499.99"))
                .currency("USD")
                .platform(bestbuy)
                .productUrl("https://bestbuy.com/playstation-5")
                .visibility("public")
                .likeCount(89)
                .commentCount(22)
                .repostCount(18)
                .createdAt(Instant.now().minus(4, ChronoUnit.DAYS))
                .updatedAt(Instant.now().minus(4, ChronoUnit.DAYS))
                .build();

        postRepository.saveAll(Arrays.asList(post1, post2, post3, post4, post5));
    }
}