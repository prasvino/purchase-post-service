package com.app.media.repo;

import com.app.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    
    /**
     * Find media associated with a specific post
     * @param postId The post ID
     * @return List of media associated with the post
     */
    @Query("SELECT m FROM Media m JOIN m.posts p WHERE p.id = :postId")
    List<Media> findByPostId(@Param("postId") UUID postId);
}
