package com.app.media.service;

import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;

public interface MediaService {
    PresignedUrlResponse createPresignedUpload(PresignedUrlRequest request);
}
