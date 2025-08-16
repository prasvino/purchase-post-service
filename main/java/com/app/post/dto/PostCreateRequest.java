package com.app.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PostCreateRequest {
    @NotBlank
    private String text;

    @NotNull
    private LocalDate purchaseDate;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String currency;

    private UUID platformId;
    private String productUrl;
    private List<UUID> mediaIds;
    private String visibility;
}
