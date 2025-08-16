package com.app.post.service;

import com.app.common.exception.NotFoundException;
import com.app.media.repo.MediaRepository;
import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import com.app.post.entity.Post;
import com.app.post.mapper.PostMapper;
import com.app.post.repo.PostRepository;
import com.app.platform.repo.PlatformRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final MediaRepository mediaRepository;
    private final PostMapper postMapper;

    // TODO: replace with security context lookup
    private User getCurrentUser() {
        return userRepository.findAll().stream().findFirst().orElseThrow(() -> new NotFoundException("No users in DB"));
    }

    @Override
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        User author = getCurrentUser();

        var platform = request.getPlatformId() == null ? null : platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new NotFoundException("Platform not found"));

        Post post = Post.builder()
                .author(author)
                .text(request.getText())
                .purchaseDate(request.getPurchaseDate())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .platform(platform)
                .productUrl(request.getProductUrl())
                .visibility(request.getVisibility() == null ? "public" : request.getVisibility())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        post = postRepository.save(post);

        // TODO: attach media (not implemented here); mediaRepository usage shown as placeholder
        if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
            // mediaRepository.findAllById(request.getMediaIds()).forEach(m -> m.setPostId(post.getId()));
            // mediaRepository.saveAll(...)
        }

        return postMapper.toDto(post);
    }

    @Override
    public PostResponse getPostById(UUID id) {
        return postRepository.findById(id).map(postMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Post not found"));
    }

    @Override
    public List<PostResponse> getUserPosts(UUID userId, int page, int size) {
        return postRepository.findByAuthorId(userId, PageRequest.of(page, size)).stream()
                .map(postMapper::toDto).collect(Collectors.toList());
    }
}
