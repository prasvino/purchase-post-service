package com.app.post.mapper;

import com.app.post.dto.PostResponse;
import com.app.user.dto.UserSummary;
import com.app.user.mapper.UserMapper;
import com.app.post.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {
    
    @Mapping(target = "user", source = "post.author")
    @Mapping(target = "content", source = "post.text")
    @Mapping(target = "timestamp", source = "post.createdAt")
    @Mapping(target = "likes", source = "post.likeCount")
    @Mapping(target = "comments", source = "post.commentCount")
    @Mapping(target = "reposts", source = "post.repostCount")
    @Mapping(target = "platform", source = "post.platform", qualifiedByName = "mapPlatform")
    @Mapping(target = "isLiked", constant = "false")
    @Mapping(target = "isReposted", constant = "false")
    @Mapping(target = "media", source = "post.productUrl")
    @Mapping(target = "tags", source = "post", qualifiedByName = "defaultTags")
    PostResponse toDto(Post post);

    @Named("mapPlatform")
    default PostResponse.Platform mapPlatform(com.app.platform.entity.Platform platform) {
        if (platform == null) {
            return null;
        }
        
        return PostResponse.Platform.builder()
                .id(platform.getId())
                .name(platform.getName())
                .icon(getPlatformIcon(platform.getName()))
                .color(getPlatformColor(platform.getName()))
                .build();
    }

    @Named("defaultTags")
    default List<String> defaultTags(Post post) {
        return Arrays.asList("purchase", "review");
    }

    private String getPlatformIcon(String platformName) {
        switch (platformName.toLowerCase()) {
            case "amazon":
                return "🛒";
            case "ebay":
                return "🏷️";
            case "best buy":
                return "🏪";
            case "etsy":
                return "🎨";
            case "target":
                return "🎯";
            default:
                return "🛍️";
        }
    }

    private String getPlatformColor(String platformName) {
        switch (platformName.toLowerCase()) {
            case "amazon":
                return "#FF9900";
            case "ebay":
                return "#E53238";
            case "best buy":
                return "#003B64";
            case "etsy":
                return "#F56400";
            case "target":
                return "#CC0000";
            default:
                return "#6B7280";
        }
    }
}
