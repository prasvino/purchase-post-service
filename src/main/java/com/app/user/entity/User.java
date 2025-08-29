package com.app.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "purchase_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    private String displayName;
    private String email;
    private String password;
    private String avatarUrl;
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
