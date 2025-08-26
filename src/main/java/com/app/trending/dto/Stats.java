package com.app.trending.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Stats {
    private Integer totalPosts;
    private Double totalMoneySpent;
    private Integer activeUsers;
}