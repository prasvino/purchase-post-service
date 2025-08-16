package com.app.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresignedUrlRequest {
    @NotBlank
    private String fileName;

    @NotBlank
    private String fileType;

    @NotNull
    private Long size;
}
