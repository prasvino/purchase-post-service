package com.app.post.service;

import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PostService {
    PostResponse createPost(PostCreateRequest request);
    PostResponse getPostById(UUID id);
    List<PostResponse> getUserPosts(UUID userId, int page, int size);
    Page<PostResponse> getAllPosts(int page, int size);
    Map<String, Object> likePost(UUID postId);
    Map<String, Object> repostPost(UUID postId);
}
