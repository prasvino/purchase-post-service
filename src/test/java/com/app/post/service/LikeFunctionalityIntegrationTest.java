package com.app.post.service;

import com.app.post.entity.Post;
import com.app.post.entity.PostLike;
import com.app.post.repo.PostRepository;
import com.app.post.repo.PostLikeRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeFunctionalityIntegrationTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private Post testPost;
    private PostLike testPostLike;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .build();

        testPost = Post.builder()
                .id(UUID.randomUUID())
                .author(testUser)
                .text("Test post")
                .likeCount(0)
                .build();

        testPostLike = PostLike.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .post(testPost)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void likePost_UserHasNotLiked_AddsLikeAndIncrementsCount() {
        // Given - User hasn't liked the post yet
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(testUser.getId(), testPost.getId()))
                .thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        Map<String, Object> result = postService.likePost(testPost.getId());

        // Then
        assertThat(result.get("liked")).isEqualTo(true);
        assertThat(result.get("likesCount")).isEqualTo(1);
        assertThat(testPost.getLikeCount()).isEqualTo(1);
        
        // Verify PostLike entity is saved
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postLikeRepository, never()).delete(any(PostLike.class));
        
        // Verify WebSocket notification for like
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void likePost_UserHasAlreadyLiked_RemovesLikeAndDecrementsCount() {
        // Given - User has already liked the post
        testPost.setLikeCount(1);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(testUser.getId(), testPost.getId()))
                .thenReturn(Optional.of(testPostLike));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        Map<String, Object> result = postService.likePost(testPost.getId());

        // Then
        assertThat(result.get("liked")).isEqualTo(false);
        assertThat(result.get("likesCount")).isEqualTo(0);
        assertThat(testPost.getLikeCount()).isEqualTo(0);
        
        // Verify PostLike entity is deleted
        verify(postLikeRepository).delete(testPostLike);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        
        // Verify WebSocket notification for unlike
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void likePost_MultipleUsers_EachCanLikeOnce() {
        // Given - First user likes the post
        User secondUser = User.builder()
                .id(UUID.randomUUID())
                .username("seconduser")
                .build();

        // Test first user like
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(testUser.getId(), testPost.getId()))
                .thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Map<String, Object> firstResult = postService.likePost(testPost.getId());
        assertThat(firstResult.get("liked")).isEqualTo(true);
        assertThat(firstResult.get("likesCount")).isEqualTo(1);

        // Reset mocks for second user
        reset(userRepository, postLikeRepository);
        testPost.setLikeCount(1);

        // Test second user like
        when(userRepository.findAll()).thenReturn(Arrays.asList(secondUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByUserIdAndPostId(secondUser.getId(), testPost.getId()))
                .thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Map<String, Object> secondResult = postService.likePost(testPost.getId());
        assertThat(secondResult.get("liked")).isEqualTo(true);
        assertThat(secondResult.get("likesCount")).isEqualTo(2);
        
        // Verify both users' likes are saved
        verify(postLikeRepository, times(2)).save(any(PostLike.class));
    }
}