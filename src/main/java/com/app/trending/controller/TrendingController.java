package com.app.trending.controller;

import com.app.trending.dto.Stats;
import com.app.trending.dto.TrendingItem;
import com.app.trending.service.TrendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrendingController {

    private final TrendingService trendingService;

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingItem>> getTrendingItems() {
        return ResponseEntity.ok(trendingService.getTrendingItems());
    }

    @GetMapping("/stats")
    public ResponseEntity<Stats> getStats() {
        return ResponseEntity.ok(trendingService.getStats());
    }
}