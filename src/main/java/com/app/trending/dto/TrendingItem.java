package com.app.trending.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class TrendingItem {
    private UUID id;
    private String name;
    private String icon;
    private String category;
    private Integer purchasesToday;
    private Integer rank;
    private Boolean isHot;
    private Boolean isRising;
}