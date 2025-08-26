package com.app.post.controller;

import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import com.app.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        // Convert to 0-based page for Spring Data
        int springPage = page - 1;
        Page<PostResponse> postsPage = postService.getAllPosts(springPage, limit);
        
        return ResponseEntity.ok(Map.of(
            "posts", postsPage.getContent(),
            "total", postsPage.getTotalElements(),
            "page", page,
            "limit", limit,
            "hasNext", postsPage.hasNext()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable UUID userId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getUserPosts(userId, page, size));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable UUID postId) {
        Map<String, Object> result = postService.likePost(postId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{postId}/repost")
    public ResponseEntity<Map<String, Object>> repostPost(@PathVariable UUID postId) {
        Map<String, Object> result = postService.repostPost(postId);
        return ResponseEntity.ok(result);
    }
}
