package com.app.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment text cannot be blank")
    private String text;
    
    private UUID parentCommentId; // For reply comments
}