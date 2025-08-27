package com.app.comment.repo;

import com.app.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    
    /**
     * Find all comments for a specific post, ordered by creation date
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdOrderByCreatedAtAsc(@Param("postId") UUID postId, Pageable pageable);
    
    /**
     * Find all reply comments for a specific parent comment
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") UUID parentCommentId);
    
    /**
     * Count total comments for a post (including replies)
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") UUID postId);
    
    /**
     * Find comments by author
     */
    @Query("SELECT c FROM Comment c WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") UUID authorId, Pageable pageable);
    
    /**
     * Find top-level comments (no parent) for a post
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") UUID postId);
}