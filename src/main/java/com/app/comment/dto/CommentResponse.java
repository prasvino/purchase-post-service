package com.app.comment.dto;

import com.app.user.dto.UserSummary;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private String text;
    private UserSummary author;
    private UUID postId;
    private UUID parentCommentId;
    private int likeCount;
    private boolean isLiked; // Whether current user liked this comment
    private List<CommentResponse> replies; // For nested comments
    private Instant createdAt;
    private Instant updatedAt;
}