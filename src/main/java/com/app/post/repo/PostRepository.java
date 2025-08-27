package com.app.post.repo;

import com.app.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByAuthorId(UUID authorId, Pageable pageable);
    
    /**
     * Search posts by keyword across multiple fields
     * Searches in post text, author username, author display name, and platform name
     */
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN p.author a " +
           "LEFT JOIN p.platform pl " +
           "WHERE LOWER(p.text) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(pl.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Search posts by multiple keywords (all keywords must match)
     */
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN p.author a " +
           "LEFT JOIN p.platform pl " +
           "WHERE (" +
           "    LOWER(p.text) LIKE LOWER(CONCAT('%', :keyword1, '%')) " +
           "    OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword1, '%')) " +
           "    OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword1, '%')) " +
           "    OR LOWER(pl.name) LIKE LOWER(CONCAT('%', :keyword1, '%'))" +
           ") " +
           "AND (" +
           "    LOWER(p.text) LIKE LOWER(CONCAT('%', :keyword2, '%')) " +
           "    OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword2, '%')) " +
           "    OR LOWER(a.displayName) LIKE LOWER(CONCAT('%', :keyword2, '%')) " +
           "    OR LOWER(pl.name) LIKE LOWER(CONCAT('%', :keyword2, '%'))" +
           ") " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchByTwoKeywords(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);
}
