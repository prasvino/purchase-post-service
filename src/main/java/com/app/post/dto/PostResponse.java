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
    private String text;
    private LocalDate purchaseDate;
    private BigDecimal price;
    private String currency;
    private UUID platformId;
    private UserSummary author;
    private String productUrl;
    private List<String> mediaUrls;
    private int likeCount;
    private int commentCount;
    private int repostCount;
    private Instant createdAt;
}
