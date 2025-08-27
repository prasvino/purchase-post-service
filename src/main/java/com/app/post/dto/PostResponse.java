package com.app.post.dto;

import com.app.user.dto.UserSummary;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class PostResponse {
    private UUID id;
    private UserSummary user;
    private String content;
    private LocalDate purchaseDate;
    private BigDecimal price;
    private String currency;
    private Platform platform;
    private String media;
    private String mediaType;
    private String location;
    private List<String> tags;
    private Instant timestamp;
    private Integer likes;
    private Integer comments;
    private Integer reposts;
    private Integer shares;
    private Boolean isLiked;
    private Boolean isReposted;
    private Boolean isShared;
    
    // Platform inner class
    @Data
    @Builder
    public static class Platform {
        private UUID id;
        private String name;
        private String icon;
        private String color;
    }
}
