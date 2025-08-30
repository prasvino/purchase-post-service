package com.app.comment.service;

import com.app.comment.dto.CommentRequest;
import com.app.comment.dto.CommentResponse;
import com.app.comment.entity.Comment;
import com.app.comment.entity.CommentLike;
import com.app.comment.repo.CommentRepository;
import com.app.comment.repo.CommentLikeRepository;
import com.app.common.exception.NotFoundException;
import com.app.common.exception.UnauthorizedException;
import com.app.post.entity.Post;
import com.app.post.repo.PostRepository;
import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import com.app.user.mapper.UserMapper;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // TODO: replace with security context lookup
    private User getCurrentUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No users in DB"));
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID postId, CommentRequest request) {
        User author = getCurrentUser();
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .text(request.getText())
                .parentComment(parentComment)
                .likeCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        comment = commentRepository.save(comment);

        // Update post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        // Send WebSocket notification for new comment
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "NEW_COMMENT",
            "payload", Map.of(
                "comment", toCommentResponse(comment),
                "postId", postId.toString()
            ),
            "timestamp", Instant.now().toString()
        ));

        return toCommentResponse(comment);
    }

    @Override
    public Page<CommentResponse> getCommentsByPostId(UUID postId, int page, int size) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(
                postId, 
                PageRequest.of(page, size)
        ).map(this::toCommentResponse);
    }

    @Override
    public List<CommentResponse> getTopLevelCommentsByPostId(UUID postId) {
        return commentRepository.findTopLevelCommentsByPostId(postId)
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getRepliesByCommentId(UUID commentId) {
        return commentRepository.findRepliesByParentCommentId(commentId)
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .map(this::toCommentResponse)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        User currentUser = getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only edit your own comments");
        }

        comment.setText(request.getText());
        comment.setUpdatedAt(Instant.now());
        comment = commentRepository.save(comment);

        return toCommentResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        User currentUser = getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        // Update post comment count
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        commentRepository.delete(comment);

        // Send WebSocket notification for comment deletion
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "COMMENT_DELETED",
            "payload", Map.of(
                "commentId", commentId.toString(),
                "postId", post.getId().toString()
            ),
            "timestamp", Instant.now().toString()
        ));
    }

    @Override
    @Transactional
    public Map<String, Object> likeComment(UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if user has already liked this comment
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId);
        
        boolean isLiked;
        int newLikeCount;
        
        if (existingLike.isPresent()) {
            // User has already liked, so unlike it
            commentLikeRepository.delete(existingLike.get());
            newLikeCount = Math.max(0, comment.getLikeCount() - 1);
            isLiked = false;
        } else {
            // User hasn't liked, so like it
            CommentLike commentLike = CommentLike.builder()
                    .user(currentUser)
                    .comment(comment)
                    .createdAt(Instant.now())
                    .build();
            commentLikeRepository.save(commentLike);
            newLikeCount = comment.getLikeCount() + 1;
            isLiked = true;
        }
        
        // Update comment like count
        comment.setLikeCount(newLikeCount);
        comment.setUpdatedAt(Instant.now());
        comment = commentRepository.save(comment);

        // Send WebSocket notification for comment like/unlike
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", isLiked ? "COMMENT_LIKED" : "COMMENT_UNLIKED",
            "payload", Map.of(
                "commentId", commentId.toString(),
                "userId", currentUser.getId().toString(),
                "likesCount", newLikeCount
            ),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "liked", isLiked,
            "likesCount", newLikeCount
        );
    }

    @Override
    public Page<CommentResponse> getCommentsByUserId(UUID userId, int page, int size) {
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(page, size)
        ).map(this::toCommentResponse);
    }

    private CommentResponse toCommentResponse(Comment comment) {
        UserSummary authorSummary = userMapper.toDto(comment.getAuthor());
        
        // Check if current user has liked this comment
        User currentUser = getCurrentUser();
        boolean isLiked = commentLikeRepository.existsByUserIdAndCommentId(currentUser.getId(), comment.getId());
        
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(authorSummary)
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .replies(null) // TODO: load replies if needed
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}