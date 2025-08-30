package com.app.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MediaResponse {
    private UUID id;
    private String fileName;
    private String fileType;
    private String url;
    private Long size;
    private String status;
    private Instant createdAt;
}