package com.app.media.controller;

import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;
import com.app.media.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/sas")
    public ResponseEntity<PresignedUrlResponse> sas(@Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(mediaService.createPresignedUpload(request));
    }
}
