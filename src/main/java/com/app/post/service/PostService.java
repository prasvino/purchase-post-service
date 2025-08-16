package com.app.post.service;

import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;

import java.util.List;
import java.util.UUID;

public interface PostService {
    PostResponse createPost(PostCreateRequest request);
    PostResponse getPostById(UUID id);
    List<PostResponse> getUserPosts(UUID userId, int page, int size);
}
