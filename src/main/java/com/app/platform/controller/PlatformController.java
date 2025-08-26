package com.app.platform.controller;

import com.app.platform.entity.Platform;
import com.app.platform.repo.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformRepository platformRepository;

    @GetMapping("/platforms")
    public ResponseEntity<List<Platform>> getPlatforms() {
        // Check if platforms exist, if not create sample data
        if (platformRepository.count() == 0) {
            createSamplePlatforms();
        }
        
        return ResponseEntity.ok(platformRepository.findAll());
    }

    private void createSamplePlatforms() {
        List<Platform> platforms = Arrays.asList(
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Amazon")
                    .domain("amazon.com")
                    .logoUrl("https://amazon.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("eBay")
                    .domain("ebay.com")
                    .logoUrl("https://ebay.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Etsy")
                    .domain("etsy.com")
                    .logoUrl("https://etsy.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Best Buy")
                    .domain("bestbuy.com")
                    .logoUrl("https://bestbuy.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Target")
                    .domain("target.com")
                    .logoUrl("https://target.com/favicon.ico")
                    .verified(true)
                    .build()
        );
        
        platformRepository.saveAll(platforms);
    }
}