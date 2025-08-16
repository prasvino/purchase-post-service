package com.app.post.entity;

import com.app.user.entity.User;
import com.app.platform.entity.Platform;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(columnDefinition = "text")
    private String text;

    private LocalDate purchaseDate;
    private BigDecimal price;
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    private String productUrl;
    private String visibility;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    private int likeCount;
    private int commentCount;
    private int repostCount;

    private Instant createdAt;
    private Instant updatedAt;
}
