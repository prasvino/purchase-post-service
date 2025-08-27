package com.app.post.controller;

import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import com.app.post.service.PostService;
import com.app.user.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private PostCreateRequest postCreateRequest;
    private PostResponse postResponse;
    private UserSummary userSummary;
    private UUID postId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userSummary = UserSummary.builder()
                .id(userId)
                .username("testuser")
                .name("Test User")
                .avatar("avatar-url")
                .build();

        postCreateRequest = new PostCreateRequest();
        postCreateRequest.setText("I just bought this amazing product!");
        postCreateRequest.setPurchaseDate(LocalDate.now());
        postCreateRequest.setPrice(new BigDecimal("99.99"));
        postCreateRequest.setCurrency("USD");
        postCreateRequest.setProductUrl("https://example.com/product");
        postCreateRequest.setVisibility("public");

        PostResponse.Platform platform = PostResponse.Platform.builder()
                .id(UUID.randomUUID())
                .name("Amazon")
                .icon("amazon-icon")
                .color("#FF9900")
                .build();

        postResponse = PostResponse.builder()
                .id(postId)
                .user(userSummary)
                .content("I just bought this amazing product!")
                .purchaseDate(LocalDate.now())
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .platform(platform)
                .timestamp(Instant.now())
                .likes(0)
                .comments(0)
                .reposts(0)
                .shares(0)
                .isLiked(false)
                .isReposted(false)
                .isShared(false)
                .build();
    }

    @Test
    void createPost_ValidRequest_ReturnsPostResponse() {
        // Given
        when(postService.createPost(any(PostCreateRequest.class))).thenReturn(postResponse);

        // When
        ResponseEntity<PostResponse> response = postController.createPost(postCreateRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("I just bought this amazing product!");
        assertThat(response.getBody().getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void getPosts_ValidPagination_ReturnsPagedPosts() {
        // Given
        List<PostResponse> posts = Arrays.asList(postResponse);
        Page<PostResponse> postsPage = new PageImpl<>(posts);
        when(postService.getAllPosts(0, 10)).thenReturn(postsPage);

        // When
        ResponseEntity<Map<String, Object>> response = postController.getPosts(1, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("posts")).isEqualTo(posts);
        assertThat(body.get("total")).isEqualTo(1L);
        assertThat(body.get("page")).isEqualTo(1);
        assertThat(body.get("limit")).isEqualTo(10);
        assertThat(body.get("hasNext")).isEqualTo(false);
    }

    @Test
    void getPosts_DefaultPagination_UsesCorrectDefaults() {
        // Given
        List<PostResponse> posts = Arrays.asList(postResponse);
        Page<PostResponse> postsPage = new PageImpl<>(posts);
        when(postService.getAllPosts(0, 10)).thenReturn(postsPage);

        // When
        ResponseEntity<Map<String, Object>> response = postController.getPosts(1, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("page")).isEqualTo(1);
        assertThat(body.get("limit")).isEqualTo(10);
    }

    @Test
    void getPost_ValidPostId_ReturnsPost() {
        // Given
        when(postService.getPostById(postId)).thenReturn(postResponse);

        // When
        ResponseEntity<PostResponse> response = postController.getPost(postId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(postId);
        assertThat(response.getBody().getContent()).isEqualTo("I just bought this amazing product!");
    }

    @Test
    void getUserPosts_ValidUserId_ReturnsUserPosts() {
        // Given
        List<PostResponse> userPosts = Arrays.asList(postResponse);
        when(postService.getUserPosts(userId, 0, 10)).thenReturn(userPosts);

        // When
        ResponseEntity<List<PostResponse>> response = postController.getUserPosts(userId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUser().getId()).isEqualTo(userId);
    }

    @Test
    void getUserPosts_DefaultPagination_UsesCorrectDefaults() {
        // Given
        List<PostResponse> userPosts = Arrays.asList(postResponse);
        when(postService.getUserPosts(userId, 0, 10)).thenReturn(userPosts);

        // When
        ResponseEntity<List<PostResponse>> response = postController.getUserPosts(userId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void likePost_ValidPostId_ReturnsLikeResult() {
        // Given
        Map<String, Object> likeResult = Map.of(
            "liked", true,
            "likesCount", 1
        );
        when(postService.likePost(postId)).thenReturn(likeResult);

        // When
        ResponseEntity<Map<String, Object>> response = postController.likePost(postId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("liked")).isEqualTo(true);
        assertThat(response.getBody().get("likesCount")).isEqualTo(1);
    }

    @Test
    void repostPost_ValidPostId_ReturnsRepostResult() {
        // Given
        Map<String, Object> repostResult = Map.of(
            "reposted", true,
            "repostsCount", 1
        );
        when(postService.repostPost(postId)).thenReturn(repostResult);

        // When
        ResponseEntity<Map<String, Object>> response = postController.repostPost(postId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("reposted")).isEqualTo(true);
        assertThat(response.getBody().get("repostsCount")).isEqualTo(1);
    }

    @Test
    void sharePost_ValidPostId_ReturnsShareResult() {
        // Given
        Map<String, Object> shareResult = Map.of(
            "shared", true,
            "sharesCount", 1
        );
        when(postService.sharePost(postId)).thenReturn(shareResult);

        // When
        ResponseEntity<Map<String, Object>> response = postController.sharePost(postId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("shared")).isEqualTo(true);
        assertThat(response.getBody().get("sharesCount")).isEqualTo(1);
    }

    @Test
    void searchPosts_ValidKeyword_ReturnsSearchResults() {
        // Given
        String keyword = "product";
        List<PostResponse> searchResults = Arrays.asList(postResponse);
        Page<PostResponse> searchPage = new PageImpl<>(searchResults);
        when(postService.searchPosts(keyword, 0, 10)).thenReturn(searchPage);

        // When
        ResponseEntity<Map<String, Object>> response = postController.searchPosts(keyword, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("posts")).isEqualTo(searchResults);
        assertThat(body.get("total")).isEqualTo(1L);
        assertThat(body.get("page")).isEqualTo(0);
        assertThat(body.get("size")).isEqualTo(10);
        assertThat(body.get("hasNext")).isEqualTo(false);
        assertThat(body.get("keyword")).isEqualTo(keyword);
    }

    @Test
    void searchPosts_NoKeyword_ReturnsAllPosts() {
        // Given
        List<PostResponse> allPosts = Arrays.asList(postResponse);
        Page<PostResponse> postsPage = new PageImpl<>(allPosts);
        when(postService.searchPosts(null, 0, 10)).thenReturn(postsPage);

        // When
        ResponseEntity<Map<String, Object>> response = postController.searchPosts(null, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("posts")).isEqualTo(allPosts);
        assertThat(body.get("keyword")).isEqualTo("");
    }

    @Test
    void searchPosts_ShortKeyword_ReturnsBadRequest() {
        // Given
        String shortKeyword = "a";

        // When
        ResponseEntity<Map<String, Object>> response = postController.searchPosts(shortKeyword, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("error")).isEqualTo("Search keyword must be at least 2 characters long");
        assertThat(body.get("posts")).isEqualTo(List.of());
        assertThat(body.get("total")).isEqualTo(0);
    }

    @Test
    void searchPosts_EmptyKeyword_ReturnsBadRequest() {
        // Given
        String emptyKeyword = "  ";

        // When
        ResponseEntity<Map<String, Object>> response = postController.searchPosts(emptyKeyword, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("error")).isEqualTo("Search keyword must be at least 2 characters long");
    }

    @Test
    void createPost_WithPlatformId_ReturnsPostWithPlatform() {
        // Given
        UUID platformId = UUID.randomUUID();
        postCreateRequest.setPlatformId(platformId);
        
        PostResponse responseWithPlatform = PostResponse.builder()
                .id(postId)
                .user(userSummary)
                .content("I just bought this amazing product!")
                .purchaseDate(LocalDate.now())
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .platform(PostResponse.Platform.builder()
                        .id(platformId)
                        .name("Amazon")
                        .icon("amazon-icon")
                        .color("#FF9900")
                        .build())
                .timestamp(Instant.now())
                .likes(0)
                .comments(0)
                .reposts(0)
                .shares(0)
                .isLiked(false)
                .isReposted(false)
                .isShared(false)
                .build();

        when(postService.createPost(any(PostCreateRequest.class))).thenReturn(responseWithPlatform);

        // When
        ResponseEntity<PostResponse> response = postController.createPost(postCreateRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlatform()).isNotNull();
        assertThat(response.getBody().getPlatform().getId()).isEqualTo(platformId);
        assertThat(response.getBody().getPlatform().getName()).isEqualTo("Amazon");
    }
}