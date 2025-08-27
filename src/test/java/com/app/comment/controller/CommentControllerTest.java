package com.app.comment.controller;

import com.app.comment.dto.CommentRequest;
import com.app.comment.dto.CommentResponse;
import com.app.comment.service.CommentService;
import com.app.user.dto.UserSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
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
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private CommentRequest commentRequest;
    private CommentResponse commentResponse;
    private UserSummary userSummary;
    private UUID postId;
    private UUID commentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userSummary = UserSummary.builder()
                .id(userId)
                .username("testuser")
                .name("Test User")
                .avatar("avatar-url")
                .build();

        commentRequest = new CommentRequest();
        commentRequest.setText("This is a test comment");
        commentRequest.setParentCommentId(null);

        commentResponse = CommentResponse.builder()
                .id(commentId)
                .text("This is a test comment")
                .author(userSummary)
                .postId(postId)
                .parentCommentId(null)
                .likeCount(0)
                .isLiked(false)
                .replies(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createComment_ValidRequest_ReturnsCommentResponse() {
        // Given
        when(commentService.createComment(eq(postId), any(CommentRequest.class))).thenReturn(commentResponse);

        // When
        ResponseEntity<CommentResponse> response = commentController.createComment(postId, commentRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getText()).isEqualTo("This is a test comment");
        assertThat(response.getBody().getAuthor().getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCommentsByPost_ValidPostId_ReturnsPagedComments() {
        // Given
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        Page<CommentResponse> commentsPage = new PageImpl<>(comments);
        when(commentService.getCommentsByPostId(postId, 0, 10)).thenReturn(commentsPage);

        // When
        ResponseEntity<Map<String, Object>> response = commentController.getCommentsByPost(postId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("comments")).isEqualTo(comments);
        assertThat(body.get("total")).isEqualTo(1L);
        assertThat(body.get("page")).isEqualTo(0);
        assertThat(body.get("size")).isEqualTo(10);
        assertThat(body.get("hasNext")).isEqualTo(false);
    }

    @Test
    void getCommentsByPost_DefaultPagination_UsesCorrectDefaults() {
        // Given
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        Page<CommentResponse> commentsPage = new PageImpl<>(comments);
        when(commentService.getCommentsByPostId(postId, 0, 10)).thenReturn(commentsPage);

        // When
        ResponseEntity<Map<String, Object>> response = commentController.getCommentsByPost(postId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("page")).isEqualTo(0);
        assertThat(body.get("size")).isEqualTo(10);
    }

    @Test
    void getTopLevelComments_ValidPostId_ReturnsCommentsList() {
        // Given
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        when(commentService.getTopLevelCommentsByPostId(postId)).thenReturn(comments);

        // When
        ResponseEntity<List<CommentResponse>> response = commentController.getTopLevelComments(postId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getText()).isEqualTo("This is a test comment");
    }

    @Test
    void getCommentReplies_ValidCommentId_ReturnsRepliesList() {
        // Given
        CommentResponse replyResponse = CommentResponse.builder()
                .id(UUID.randomUUID())
                .text("This is a reply")
                .author(userSummary)
                .postId(postId)
                .parentCommentId(commentId)
                .likeCount(0)
                .isLiked(false)
                .replies(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        List<CommentResponse> replies = Arrays.asList(replyResponse);
        when(commentService.getRepliesByCommentId(commentId)).thenReturn(replies);

        // When
        ResponseEntity<List<CommentResponse>> response = commentController.getCommentReplies(commentId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getText()).isEqualTo("This is a reply");
        assertThat(response.getBody().get(0).getParentCommentId()).isEqualTo(commentId);
    }

    @Test
    void getComment_ValidCommentId_ReturnsComment() {
        // Given
        when(commentService.getCommentById(commentId)).thenReturn(commentResponse);

        // When
        ResponseEntity<CommentResponse> response = commentController.getComment(commentId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(commentId);
        assertThat(response.getBody().getText()).isEqualTo("This is a test comment");
    }

    @Test
    void updateComment_ValidRequest_ReturnsUpdatedComment() {
        // Given
        CommentRequest updateRequest = new CommentRequest();
        updateRequest.setText("Updated comment text");
        
        CommentResponse updatedResponse = CommentResponse.builder()
                .id(commentId)
                .text("Updated comment text")
                .author(userSummary)
                .postId(postId)
                .parentCommentId(null)
                .likeCount(0)
                .isLiked(false)
                .replies(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(commentService.updateComment(eq(commentId), any(CommentRequest.class))).thenReturn(updatedResponse);

        // When
        ResponseEntity<CommentResponse> response = commentController.updateComment(commentId, updateRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getText()).isEqualTo("Updated comment text");
    }

    @Test
    void deleteComment_ValidCommentId_ReturnsNoContent() {
        // Given
        doNothing().when(commentService).deleteComment(commentId);

        // When
        ResponseEntity<Void> response = commentController.deleteComment(commentId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void likeComment_ValidCommentId_ReturnsLikeResult() {
        // Given
        Map<String, Object> likeResult = Map.of(
            "liked", true,
            "likesCount", 1
        );
        when(commentService.likeComment(commentId)).thenReturn(likeResult);

        // When
        ResponseEntity<Map<String, Object>> response = commentController.likeComment(commentId);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("liked")).isEqualTo(true);
        assertThat(response.getBody().get("likesCount")).isEqualTo(1);
    }

    @Test
    void getUserComments_ValidUserId_ReturnsPagedComments() {
        // Given
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        Page<CommentResponse> commentsPage = new PageImpl<>(comments);
        when(commentService.getCommentsByUserId(userId, 0, 10)).thenReturn(commentsPage);

        // When
        ResponseEntity<Map<String, Object>> response = commentController.getUserComments(userId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("comments")).isEqualTo(comments);
        assertThat(body.get("total")).isEqualTo(1L);
        assertThat(body.get("page")).isEqualTo(0);
        assertThat(body.get("size")).isEqualTo(10);
        assertThat(body.get("hasNext")).isEqualTo(false);
    }

    @Test
    void getUserComments_DefaultPagination_UsesCorrectDefaults() {
        // Given
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        Page<CommentResponse> commentsPage = new PageImpl<>(comments);
        when(commentService.getCommentsByUserId(userId, 0, 10)).thenReturn(commentsPage);

        // When - Call without explicit page/size parameters (they will use defaults from @RequestParam)
        ResponseEntity<Map<String, Object>> response = commentController.getUserComments(userId, 0, 10);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("page")).isEqualTo(0);
        assertThat(body.get("size")).isEqualTo(10);
    }

    @Test
    void createComment_ReplyComment_HasParentId() {
        // Given
        UUID parentCommentId = UUID.randomUUID();
        CommentRequest replyRequest = new CommentRequest();
        replyRequest.setText("This is a reply comment");
        replyRequest.setParentCommentId(parentCommentId);

        CommentResponse replyResponse = CommentResponse.builder()
                .id(UUID.randomUUID())
                .text("This is a reply comment")
                .author(userSummary)
                .postId(postId)
                .parentCommentId(parentCommentId)
                .likeCount(0)
                .isLiked(false)
                .replies(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(commentService.createComment(eq(postId), any(CommentRequest.class))).thenReturn(replyResponse);

        // When
        ResponseEntity<CommentResponse> response = commentController.createComment(postId, replyRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getParentCommentId()).isEqualTo(parentCommentId);
        assertThat(response.getBody().getText()).isEqualTo("This is a reply comment");
    }
}