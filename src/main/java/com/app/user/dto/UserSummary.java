package com.app.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class UserSummary {
    private UUID id;
    private String username;
    private String displayName;
    private String avatarUrl;
}
