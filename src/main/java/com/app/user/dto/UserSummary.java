package com.app.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UserSummary {
    private UUID id;
    private String name;
    private String username;
    private String avatar;
    private String bio;
    private String location;
    private String website;
    private Instant joinedAt;
    private Boolean isVerified;
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    private Double totalSpent;
    private Double avgRating;
    private Boolean isOnline;
}
