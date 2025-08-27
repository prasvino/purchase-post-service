package com.app.comment.service;

import com.app.comment.dto.CommentRequest;
import com.app.comment.dto.CommentResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CommentService {
    
    /**
     * Create a new comment on a post
     */
    CommentResponse createComment(UUID postId, CommentRequest request);
    
    /**
     * Get all comments for a specific post with pagination
     */
    Page<CommentResponse> getCommentsByPostId(UUID postId, int page, int size);
    
    /**
     * Get all top-level comments for a post (no replies)
     */
    List<CommentResponse> getTopLevelCommentsByPostId(UUID postId);
    
    /**
     * Get replies for a specific comment
     */
    List<CommentResponse> getRepliesByCommentId(UUID commentId);
    
    /**
     * Get a specific comment by ID
     */
    CommentResponse getCommentById(UUID commentId);
    
    /**
     * Update a comment (only by original author)
     */
    CommentResponse updateComment(UUID commentId, CommentRequest request);
    
    /**
     * Delete a comment (only by original author)
     */
    void deleteComment(UUID commentId);
    
    /**
     * Like/unlike a comment
     */
    Map<String, Object> likeComment(UUID commentId);
    
    /**
     * Get comments by a specific user
     */
    Page<CommentResponse> getCommentsByUserId(UUID userId, int page, int size);
}