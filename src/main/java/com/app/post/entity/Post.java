package com.app.post.entity;

import com.app.user.entity.User;
import com.app.platform.entity.Platform;
import com.app.media.entity.Media;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts", schema = "purchase_service")
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
    private int shareCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_media",
        schema = "purchase_service",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private List<Media> media;

    private Instant createdAt;
    private Instant updatedAt;
}
