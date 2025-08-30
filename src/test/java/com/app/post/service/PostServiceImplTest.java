package com.app.post.service;

import com.app.common.exception.NotFoundException;
import com.app.media.repo.MediaRepository;
import com.app.media.service.MediaService;
import com.app.media.entity.Media;
import com.app.platform.entity.Platform;
import com.app.platform.repo.PlatformRepository;
import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import com.app.post.entity.Post;
import com.app.post.mapper.PostMapper;
import com.app.post.repo.PostRepository;
import com.app.post.repo.PostLikeRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private PostMapper postMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private Platform testPlatform;
    private Post testPost;
    private PostCreateRequest postCreateRequest;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .build();

        testPlatform = Platform.builder()
                .id(UUID.randomUUID())
                .name("Amazon")
                .logoUrl("amazon-logo")
                .domain("amazon.com")
                .verified(true)
                .build();

        testPost = Post.builder()
                .id(UUID.randomUUID())
                .author(testUser)
                .text("I just bought this amazing product!")
                .purchaseDate(LocalDate.now())
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .platform(testPlatform)
                .productUrl("https://example.com/product")
                .visibility("public")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .likeCount(0)
                .commentCount(0)
                .repostCount(0)
                .shareCount(0)
                .build();

        postCreateRequest = new PostCreateRequest();
        postCreateRequest.setText("I just bought this amazing product!");
        postCreateRequest.setPurchaseDate(LocalDate.now());
        postCreateRequest.setPrice(new BigDecimal("99.99"));
        postCreateRequest.setCurrency("USD");
        postCreateRequest.setPlatformId(testPlatform.getId());
        postCreateRequest.setProductUrl("https://example.com/product");
        postCreateRequest.setVisibility("public");

        postResponse = PostResponse.builder()
                .id(testPost.getId())
                .content("I just bought this amazing product!")
                .purchaseDate(LocalDate.now())
                .price(new BigDecimal("99.99"))
                .currency("USD")
                .timestamp(Instant.now())
                .likes(0)
                .comments(0)
                .reposts(0)
                .shares(0)
                .isLiked(false)
                .isReposted(false)
                .isShared(false)
                .build();
        
        // Default mock for like existence check
        lenient().when(postLikeRepository.existsByUserIdAndPostId(any(), any())).thenReturn(false);
    }

    @Test
    void createPost_ValidRequest_ReturnsPostResponse() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(platformRepository.findById(testPlatform.getId())).thenReturn(Optional.of(testPlatform));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(postCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("I just bought this amazing product!");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(result.getCurrency()).isEqualTo("USD");

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void createPost_WithoutPlatform_ReturnsPostResponse() {
        // Given
        postCreateRequest.setPlatformId(null);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(postCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("I just bought this amazing product!");
        
        // Verify platform repository was not called
        verifyNoInteractions(platformRepository);
    }

    @Test
    void createPost_PlatformNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPlatformId = UUID.randomUUID();
        postCreateRequest.setPlatformId(nonExistentPlatformId);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(platformRepository.findById(nonExistentPlatformId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(postCreateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Platform not found");
    }

    @Test
    void createPost_NoUsersInDB_ThrowsNotFoundException() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(postCreateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No users in DB");
    }

    @Test
    void createPost_SetsCorrectDefaults() {
        // Given
        postCreateRequest.setVisibility(null); // Test default visibility
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(platformRepository.findById(testPlatform.getId())).thenReturn(Optional.of(testPlatform));
        when(postMapper.toDto(any(Post.class))).thenReturn(postResponse);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        when(postRepository.save(postCaptor.capture())).thenReturn(testPost);

        // When
        postService.createPost(postCreateRequest);

        // Then
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getVisibility()).isEqualTo("public");
        assertThat(savedPost.getLikeCount()).isEqualTo(0);
        assertThat(savedPost.getCommentCount()).isEqualTo(0);
        assertThat(savedPost.getRepostCount()).isEqualTo(0);
        assertThat(savedPost.getShareCount()).isEqualTo(0);
        assertThat(savedPost.getCreatedAt()).isNotNull();
        assertThat(savedPost.getUpdatedAt()).isNotNull();
    }

    @Test
    void getPostById_ValidPostId_ReturnsPost() {
        // Given
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        PostResponse result = postService.getPostById(testPost.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPost.getId());
        assertThat(result.getContent()).isEqualTo("I just bought this amazing product!");
    }

    @Test
    void getPostById_PostNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostById(nonExistentPostId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void getUserPosts_ValidUserId_ReturnsUserPosts() {
        // Given
        List<Post> userPosts = Arrays.asList(testPost);
        Page<Post> userPostsPage = new PageImpl<>(userPosts);
        when(postRepository.findByAuthorId(testUser.getId(), PageRequest.of(0, 10))).thenReturn(userPostsPage);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        List<PostResponse> result = postService.getUserPosts(testUser.getId(), 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("I just bought this amazing product!");
    }

    @Test
    void getAllPosts_ValidPagination_ReturnsPagedPosts() {
        // Given
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postsPage = new PageImpl<>(posts);
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        when(postRepository.findAll(expectedPageRequest)).thenReturn(postsPage);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        Page<PostResponse> result = postService.getAllPosts(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("I just bought this amazing product!");
    }

    @Test
    void likePost_ValidPost_ReturnsLikeResult() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(testUser.getId(), testPost.getId())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        Map<String, Object> result = postService.likePost(testPost.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("liked")).isEqualTo(true);
        assertThat(result.get("likesCount")).isEqualTo(1);
        assertThat(testPost.getLikeCount()).isEqualTo(1);

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
        
        // Verify PostLike is saved
        verify(postLikeRepository).save(any());
    }

    @Test
    void likePost_PostNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.likePost(nonExistentPostId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void repostPost_ValidPost_ReturnsRepostResult() {
        // Given
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // When
        Map<String, Object> result = postService.repostPost(testPost.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("reposted")).isEqualTo(true);
        assertThat(result.get("repostsCount")).isEqualTo(1);
        assertThat(testPost.getRepostCount()).isEqualTo(1);

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void repostPost_PostNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.repostPost(nonExistentPostId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void sharePost_ValidPost_ReturnsShareResult() {
        // Given
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // When
        Map<String, Object> result = postService.sharePost(testPost.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("shared")).isEqualTo(true);
        assertThat(result.get("sharesCount")).isEqualTo(1);
        assertThat(testPost.getShareCount()).isEqualTo(1);

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void sharePost_PostNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.sharePost(nonExistentPostId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void searchPosts_ValidKeyword_ReturnsSearchResults() {
        // Given
        String keyword = "product";
        List<Post> searchResults = Arrays.asList(testPost);
        Page<Post> searchPage = new PageImpl<>(searchResults);
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        when(postRepository.searchByKeyword(keyword, expectedPageRequest)).thenReturn(searchPage);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        Page<PostResponse> result = postService.searchPosts(keyword, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("I just bought this amazing product!");
    }

    @Test
    void searchPosts_NullKeyword_ReturnsAllPosts() {
        // Given
        List<Post> allPosts = Arrays.asList(testPost);
        Page<Post> postsPage = new PageImpl<>(allPosts);
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        when(postRepository.findAll(expectedPageRequest)).thenReturn(postsPage);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        Page<PostResponse> result = postService.searchPosts(null, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findAll(expectedPageRequest);
        verify(postRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void searchPosts_EmptyKeyword_ReturnsAllPosts() {
        // Given
        List<Post> allPosts = Arrays.asList(testPost);
        Page<Post> postsPage = new PageImpl<>(allPosts);
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        when(postRepository.findAll(expectedPageRequest)).thenReturn(postsPage);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        Page<PostResponse> result = postService.searchPosts("  ", 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findAll(expectedPageRequest);
        verify(postRepository, never()).searchByKeyword(any(), any());
    }

    @Test
    void likePost_UpdatesTimestamp() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(testUser.getId(), testPost.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        when(postRepository.save(postCaptor.capture())).thenReturn(testPost);

        // When
        postService.likePost(testPost.getId());

        // Then
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getUpdatedAt()).isNotNull();
        // Verify the timestamp was updated (it should be set to current time)
        assertThat(savedPost.getUpdatedAt()).isAfterOrEqualTo(Instant.now().minusSeconds(1));
    }

    @Test
    void createPost_WithMediaIds_ProcessesCorrectly() {
        // Given
        List<UUID> mediaIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        postCreateRequest.setMediaIds(mediaIds);
        
        List<Media> mockMedia = Arrays.asList(
            Media.builder().id(mediaIds.get(0)).build(),
            Media.builder().id(mediaIds.get(1)).build()
        );
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(platformRepository.findById(testPlatform.getId())).thenReturn(Optional.of(testPlatform));
        when(mediaService.getValidatedMediaByIds(mediaIds)).thenReturn(mockMedia);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(postMapper.toDto(testPost)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(postCreateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(mediaService).getValidatedMediaByIds(mediaIds);
        // Verify the post was saved twice (initial save + save with media)
        verify(postRepository, times(2)).save(any(Post.class));
    }
}