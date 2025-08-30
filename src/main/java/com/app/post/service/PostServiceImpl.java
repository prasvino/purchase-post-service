package com.app.post.service;

import com.app.common.exception.NotFoundException;
import com.app.media.repo.MediaRepository;
import com.app.media.service.MediaService;
import com.app.media.entity.Media;
import com.app.post.dto.PostCreateRequest;
import com.app.post.dto.PostResponse;
import com.app.post.entity.Post;
import com.app.post.entity.PostLike;
import com.app.post.mapper.PostMapper;
import com.app.post.repo.PostRepository;
import com.app.post.repo.PostLikeRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PlatformRepository platformRepository;
    private final MediaService mediaService;
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
                .shareCount(0)
                .build();

        post = postRepository.save(post);

        // Associate media with the post
        if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
            List<Media> validatedMedia = mediaService.getValidatedMediaByIds(request.getMediaIds());
            post.setMedia(validatedMedia);
            post = postRepository.save(post);
        }

        // Send WebSocket notification for new post
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "NEW_POST",
            "payload", Map.of("post", postMapper.toDto(post)),
            "timestamp", Instant.now().toString()
        ));

        PostResponse response = postMapper.toDto(post);
        setLikeStatus(response);
        return response;
    }

    @Override
    public PostResponse getPostById(UUID id) {
        PostResponse response = postRepository.findById(id).map(postMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Set the like status for current user
        setLikeStatus(response);
        return response;
    }

    @Override
    public List<PostResponse> getUserPosts(UUID userId, int page, int size) {
        List<PostResponse> posts = postRepository.findByAuthorId(userId, PageRequest.of(page, size)).stream()
                .map(postMapper::toDto).collect(Collectors.toList());
        
        // Set like status for each post
        posts.forEach(this::setLikeStatus);
        return posts;
    }

    @Override
    public Page<PostResponse> getAllPosts(int page, int size) {
        Page<PostResponse> postsPage = postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(postMapper::toDto);
        
        // Set like status for each post
        postsPage.forEach(this::setLikeStatus);
        return postsPage;
    }

    @Override
    @Transactional
    public Map<String, Object> likePost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Check if user has already liked this post
        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(currentUser.getId(), postId);
        
        boolean isLiked;
        int newLikeCount;
        
        if (existingLike.isPresent()) {
            // User has already liked, so unlike it
            postLikeRepository.delete(existingLike.get());
            newLikeCount = Math.max(0, post.getLikeCount() - 1);
            isLiked = false;
        } else {
            // User hasn't liked, so like it
            PostLike postLike = PostLike.builder()
                    .user(currentUser)
                    .post(post)
                    .createdAt(Instant.now())
                    .build();
            postLikeRepository.save(postLike);
            newLikeCount = post.getLikeCount() + 1;
            isLiked = true;
        }
        
        // Update post like count
        post.setLikeCount(newLikeCount);
        post.setUpdatedAt(Instant.now());
        post = postRepository.save(post);

        // Send WebSocket notification for post like/unlike
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", isLiked ? "POST_LIKED" : "POST_UNLIKED",
            "payload", Map.of(
                "postId", postId.toString(),
                "userId", currentUser.getId().toString(),
                "likesCount", newLikeCount
            ),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "liked", isLiked,
            "likesCount", newLikeCount
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

    @Override
    @Transactional
    public Map<String, Object> sharePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        post.setShareCount(post.getShareCount() + 1);
        post.setUpdatedAt(Instant.now());
        post = postRepository.save(post);

        // Send WebSocket notification for post share
        messagingTemplate.convertAndSend("/topic/posts", Map.of(
            "type", "POST_SHARED",
            "payload", Map.of(
                "postId", postId.toString(),
                "userId", getCurrentUser().getId().toString(),
                "sharesCount", post.getShareCount()
            ),
            "timestamp", Instant.now().toString()
        ));

        return Map.of(
            "shared", true,
            "sharesCount", post.getShareCount()
        );
    }

    @Override
    public Page<PostResponse> searchPosts(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // If no keyword provided, return all posts
            return getAllPosts(page, size);
        }
        
        // Clean and prepare the keyword
        String cleanKeyword = keyword.trim();
        
        // Check if keyword contains multiple words
        String[] keywords = cleanKeyword.split("\\s+");
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<PostResponse> postsPage;
        if (keywords.length == 1) {
            // Single keyword search
            postsPage = postRepository.searchByKeyword(cleanKeyword, pageRequest)
                    .map(postMapper::toDto);
        } else if (keywords.length == 2) {
            // Two keywords search (both must match)
            postsPage = postRepository.searchByTwoKeywords(keywords[0], keywords[1], pageRequest)
                    .map(postMapper::toDto);
        } else {
            // For more than 2 keywords, search with the first keyword only
            // This can be enhanced later for more complex multi-keyword searches
            postsPage = postRepository.searchByKeyword(keywords[0], pageRequest)
                    .map(postMapper::toDto);
        }
        
        // Set like status for each post
        postsPage.forEach(this::setLikeStatus);
        return postsPage;
    }
    
    /**
     * Helper method to set the like status for a post based on current user
     */
    private void setLikeStatus(PostResponse postResponse) {
        try {
            User currentUser = getCurrentUser();
            boolean isLiked = postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), postResponse.getId());
            postResponse.setIsLiked(isLiked);
        } catch (Exception e) {
            // If there's no current user or any error, default to false
            postResponse.setIsLiked(false);
        }
    }
}
