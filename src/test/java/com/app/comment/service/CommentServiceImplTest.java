package com.app.comment.service;

import com.app.comment.dto.CommentRequest;
import com.app.comment.dto.CommentResponse;
import com.app.comment.entity.Comment;
import com.app.comment.repo.CommentRepository;
import com.app.common.exception.NotFoundException;
import com.app.common.exception.UnauthorizedException;
import com.app.post.entity.Post;
import com.app.post.repo.PostRepository;
import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.mapper.UserMapper;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
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

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private CommentRequest commentRequest;
    private UserSummary userSummary;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .build();

        testPost = Post.builder()
                .id(UUID.randomUUID())
                .author(testUser)
                .text("Test post")
                .commentCount(1)
                .build();

        testComment = Comment.builder()
                .id(UUID.randomUUID())
                .post(testPost)
                .author(testUser)
                .text("Test comment")
                .parentComment(null)
                .likeCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        commentRequest = new CommentRequest();
        commentRequest.setText("Test comment");
        commentRequest.setParentCommentId(null);

        userSummary = UserSummary.builder()
                .id(testUser.getId())
                .username("testuser")
                .name("Test User")
                .avatar("avatar-url")
                .build();
    }

    @Test
    void createComment_ValidRequest_ReturnsCommentResponse() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        CommentResponse result = commentService.createComment(testPost.getId(), commentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthor().getUsername()).isEqualTo("testuser");
        assertThat(result.getPostId()).isEqualTo(testPost.getId());

        // Verify post comment count is incremented
        verify(postRepository).save(testPost);
        assertThat(testPost.getCommentCount()).isEqualTo(2);

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void createComment_ReplyComment_ValidParentId_ReturnsCommentResponse() {
        // Given
        Comment parentComment = Comment.builder()
                .id(UUID.randomUUID())
                .post(testPost)
                .author(testUser)
                .text("Parent comment")
                .build();

        CommentRequest replyRequest = new CommentRequest();
        replyRequest.setText("Reply comment");
        replyRequest.setParentCommentId(parentComment.getId());

        Comment replyComment = Comment.builder()
                .id(UUID.randomUUID())
                .post(testPost)
                .author(testUser)
                .text("Reply comment")
                .parentComment(parentComment)
                .likeCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        CommentResponse result = commentService.createComment(testPost.getId(), replyRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Reply comment");
        assertThat(result.getParentCommentId()).isEqualTo(parentComment.getId());
    }

    @Test
    void createComment_PostNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(nonExistentPostId, commentRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void createComment_ParentCommentNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentParentId = UUID.randomUUID();
        commentRequest.setParentCommentId(nonExistentParentId);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(nonExistentParentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testPost.getId(), commentRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Parent comment not found");
    }

    @Test
    void getCommentsByPostId_ValidPostId_ReturnsPagedComments() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentsPage = new PageImpl<>(comments);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(eq(testPost.getId()), any(PageRequest.class)))
                .thenReturn(commentsPage);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        Page<CommentResponse> result = commentService.getCommentsByPostId(testPost.getId(), 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getText()).isEqualTo("Test comment");
    }

    @Test
    void getTopLevelCommentsByPostId_ValidPostId_ReturnsTopLevelComments() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findTopLevelCommentsByPostId(testPost.getId())).thenReturn(comments);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        List<CommentResponse> result = commentService.getTopLevelCommentsByPostId(testPost.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
        assertThat(result.get(0).getParentCommentId()).isNull();
    }

    @Test
    void getRepliesByCommentId_ValidCommentId_ReturnsReplies() {
        // Given
        Comment replyComment = Comment.builder()
                .id(UUID.randomUUID())
                .post(testPost)
                .author(testUser)
                .text("Reply comment")
                .parentComment(testComment)
                .likeCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        List<Comment> replies = Arrays.asList(replyComment);
        when(commentRepository.findRepliesByParentCommentId(testComment.getId())).thenReturn(replies);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        List<CommentResponse> result = commentService.getRepliesByCommentId(testComment.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Reply comment");
        assertThat(result.get(0).getParentCommentId()).isEqualTo(testComment.getId());
    }

    @Test
    void getCommentById_ValidCommentId_ReturnsComment() {
        // Given
        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        CommentResponse result = commentService.getCommentById(testComment.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testComment.getId());
        assertThat(result.getText()).isEqualTo("Test comment");
    }

    @Test
    void getCommentById_CommentNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentCommentId = UUID.randomUUID();
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.getCommentById(nonExistentCommentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Comment not found");
    }

    @Test
    void updateComment_ValidRequest_ReturnsUpdatedComment() {
        // Given
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setText("Updated comment text");

        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        CommentResponse result = commentService.updateComment(testComment.getId(), updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).save(testComment);
        assertThat(testComment.getText()).isEqualTo("Updated comment text");
    }

    @Test
    void updateComment_UnauthorizedUser_ThrowsUnauthorizedException() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("otheruser")
                .build();

        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setText("Updated comment text");

        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(userRepository.findAll()).thenReturn(Arrays.asList(otherUser));

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(testComment.getId(), updateRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You can only edit your own comments");
    }

    @Test
    void deleteComment_ValidComment_DeletesSuccessfully() {
        // Given
        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // When
        commentService.deleteComment(testComment.getId());

        // Then
        verify(commentRepository).delete(testComment);
        verify(postRepository).save(testPost);
        assertThat(testPost.getCommentCount()).isEqualTo(0); // Decremented from 1

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void deleteComment_UnauthorizedUser_ThrowsUnauthorizedException() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("otheruser")
                .build();

        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(userRepository.findAll()).thenReturn(Arrays.asList(otherUser));

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(testComment.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You can only delete your own comments");
    }

    @Test
    void likeComment_ValidComment_ReturnsLikeResult() {
        // Given
        when(commentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        // When
        Map<String, Object> result = commentService.likeComment(testComment.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("liked")).isEqualTo(true);
        assertThat(result.get("likesCount")).isEqualTo(1);
        assertThat(testComment.getLikeCount()).isEqualTo(1);

        // Verify WebSocket notification is sent
        verify(messagingTemplate).convertAndSend(eq("/topic/posts"), any(Map.class));
    }

    @Test
    void likeComment_CommentNotFound_ThrowsNotFoundException() {
        // Given
        UUID nonExistentCommentId = UUID.randomUUID();
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.likeComment(nonExistentCommentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Comment not found");
    }

    @Test
    void getCommentsByUserId_ValidUserId_ReturnsPagedComments() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentsPage = new PageImpl<>(comments);
        when(commentRepository.findByAuthorIdOrderByCreatedAtDesc(eq(testUser.getId()), any(PageRequest.class)))
                .thenReturn(commentsPage);
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        // When
        Page<CommentResponse> result = commentService.getCommentsByUserId(testUser.getId(), 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthor().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void getCurrentUser_NoUsersInDB_ThrowsNotFoundException() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testPost.getId(), commentRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No users in DB");
    }

    @Test
    void createComment_SetsCorrectTimestamps() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(userMapper.toDto(testUser)).thenReturn(userSummary);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        when(commentRepository.save(commentCaptor.capture())).thenReturn(testComment);

        // When
        commentService.createComment(testPost.getId(), commentRequest);

        // Then
        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getCreatedAt()).isNotNull();
        assertThat(savedComment.getUpdatedAt()).isNotNull();
        assertThat(savedComment.getLikeCount()).isEqualTo(0);
    }
}