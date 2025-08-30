package com.app.comment.repo;

import com.app.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
    
    /**
     * Find a specific like by user and comment
     */
    Optional<CommentLike> findByUserIdAndCommentId(UUID userId, UUID commentId);
    
    /**
     * Check if a user has liked a specific comment
     */
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);
    
    /**
     * Get all likes for a specific comment
     */
    List<CommentLike> findByCommentId(UUID commentId);
    
    /**
     * Get all likes by a specific user
     */
    List<CommentLike> findByUserId(UUID userId);
    
    /**
     * Count total likes for a specific comment
     */
    long countByCommentId(UUID commentId);
    
    /**
     * Delete like by user and comment
     */
    void deleteByUserIdAndCommentId(UUID userId, UUID commentId);
    
    /**
     * Get comment IDs that a user has liked
     */
    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId")
    List<UUID> findLikedCommentIdsByUserId(@Param("userId") UUID userId);
}