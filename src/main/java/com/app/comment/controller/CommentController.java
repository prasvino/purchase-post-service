package com.app.comment.controller;

import com.app.comment.dto.CommentRequest;
import com.app.comment.dto.CommentResponse;
import com.app.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Create a new comment on a post
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(postId, request));
    }

    /**
     * Get all comments for a post with pagination
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getCommentsByPost(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentResponse> commentsPage = commentService.getCommentsByPostId(postId, page, size);
        
        return ResponseEntity.ok(Map.of(
            "comments", commentsPage.getContent(),
            "total", commentsPage.getTotalElements(),
            "page", page,
            "size", size,
            "hasNext", commentsPage.hasNext()
        ));
    }

    /**
     * Get top-level comments for a post (no replies)
     */
    @GetMapping("/posts/{postId}/comments/top-level")
    public ResponseEntity<List<CommentResponse>> getTopLevelComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getTopLevelCommentsByPostId(postId));
    }

    /**
     * Get replies for a specific comment
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getCommentReplies(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.getRepliesByCommentId(commentId));
    }

    /**
     * Get a specific comment by ID
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    /**
     * Update a comment (only by original author)
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    /**
     * Delete a comment (only by original author)
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Like/unlike a comment
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Map<String, Object>> likeComment(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.likeComment(commentId));
    }

    /**
     * Get comments by a specific user
     */
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Map<String, Object>> getUserComments(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentResponse> commentsPage = commentService.getCommentsByUserId(userId, page, size);
        
        return ResponseEntity.ok(Map.of(
            "comments", commentsPage.getContent(),
            "total", commentsPage.getTotalElements(),
            "page", page,
            "size", size,
            "hasNext", commentsPage.hasNext()
        ));
    }
}