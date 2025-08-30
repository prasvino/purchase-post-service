package com.app.post.repo;

import com.app.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    
    /**
     * Find a specific like by user and post
     */
    Optional<PostLike> findByUserIdAndPostId(UUID userId, UUID postId);
    
    /**
     * Check if a user has liked a specific post
     */
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
    
    /**
     * Get all likes for a specific post
     */
    List<PostLike> findByPostId(UUID postId);
    
    /**
     * Get all likes by a specific user
     */
    List<PostLike> findByUserId(UUID userId);
    
    /**
     * Count total likes for a specific post
     */
    long countByPostId(UUID postId);
    
    /**
     * Delete like by user and post
     */
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
    
    /**
     * Get post IDs that a user has liked
     */
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId")
    List<UUID> findLikedPostIdsByUserId(@Param("userId") UUID userId);
}