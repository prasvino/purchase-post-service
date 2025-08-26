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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private final SimpMessagingTemplate messagingTemplate;

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
                .likeCount(0)
                .commentCount(0)
                .repostCount(0)
                .build();

        post = postRepository.save(post);

        // TODO: attach media (not implemented here); mediaRepository usage shown as placeholder
        if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
            // mediaRepository.findAllById(request.getMediaIds()).forEach(m -> m.setPostId(post.getId()));
            // mediaRepository.saveAll(...)
        }

        // Send WebSocket notification for new post
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "NEW_POST",
            "payload", Map.of("post", postMapper.toDto(post)),
            "timestamp", Instant.now().toString()
        ));

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

    @Override
    public Page<PostResponse> getAllPosts(int page, int size) {
        return postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(postMapper::toDto);
    }

    @Override
    @Transactional
    public Map<String, Object> likePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        post.setLikeCount(post.getLikeCount() + 1);
        post.setUpdatedAt(Instant.now());
        post = postRepository.save(post);

        // Send WebSocket notification for post like
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "POST_LIKED",
            "payload", Map.of(
                "postId", postId.toString(),
                "userId", getCurrentUser().getId().toString(),
                "likesCount", post.getLikeCount()
            ),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "liked", true,
            "likesCount", post.getLikeCount()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> repostPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        post.setRepostCount(post.getRepostCount() + 1);
        post.setUpdatedAt(Instant.now());
        post = postRepository.save(post);

        // Send WebSocket notification for post repost
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "POST_REPOSTED",
            "payload", Map.of(
                "postId", postId.toString(),
                "userId", getCurrentUser().getId().toString(),
                "repostsCount", post.getRepostCount()
            ),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "reposted", true,
            "repostsCount", post.getRepostCount()
        );
    }
}
