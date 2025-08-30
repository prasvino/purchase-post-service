package com.app.media.service;

import com.app.common.exception.NotFoundException;
import com.app.media.azure.AzureSasUrlGenerator;
import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;
import com.app.media.entity.Media;
import com.app.media.repo.MediaRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final AzureSasUrlGenerator generator;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    // TODO: replace with real security principal lookup
    private User getCurrentUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No user in DB"));
    }

    @Override
    @Transactional
    public PresignedUrlResponse createPresignedUpload(PresignedUrlRequest request) {
        User uploader = getCurrentUser();

        Media media = Media.builder()
                .uploader(uploader)
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .size(request.getSize())
                .status("UPLOADED")
                .createdAt(Instant.now())
                .build();
        media = mediaRepository.save(media);

        String key = String.format("uploads/%s/%s-%s", uploader.getId(), media.getId(), request.getFileName());
        String uploadUrl = generator.generateUploadUrl(key, request.getFileType());
        String fileUrl = generator.getPublicFileUrl(key);

        media.setUrl(fileUrl);
        mediaRepository.save(media);

        return PresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .fileUrl(fileUrl)
                .mediaId(media.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> getValidatedMediaByIds(List<UUID> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return List.of();
        }

        User currentUser = getCurrentUser();
        List<Media> mediaList = mediaRepository.findAllById(mediaIds);
        
        // Validate that all requested media IDs were found
        if (mediaList.size() != mediaIds.size()) {
            throw new NotFoundException("Some media files not found");
        }
        
        // Validate that all media belongs to the current user
        for (Media media : mediaList) {
            if (!media.getUploader().getId().equals(currentUser.getId())) {
                throw new NotFoundException("Media not found or access denied");
            }
        }
        
        return mediaList;
    }
}
