package com.app.media.service;

import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;
import com.app.media.entity.Media;

import java.util.List;
import java.util.UUID;

public interface MediaService {
    PresignedUrlResponse createPresignedUpload(PresignedUrlRequest request);
    
    /**
     * Retrieve and validate media by IDs for the current user
     * @param mediaIds List of media IDs to retrieve
     * @return List of valid Media entities owned by current user
     * @throws com.app.common.exception.NotFoundException if any media not found or not owned by user
     */
    List<Media> getValidatedMediaByIds(List<UUID> mediaIds);
}
