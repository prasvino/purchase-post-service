package com.app.media.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class PresignedUrlResponse {
    private String uploadUrl; // presigned PUT
    private String fileUrl;   // public file url or CDN url
    private UUID mediaId;
}
