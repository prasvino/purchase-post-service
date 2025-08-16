package com.app.media.service;

import com.app.common.exception.NotFoundException;
import com.app.media.aws.S3PresignedUrlGenerator;
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

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final S3PresignedUrlGenerator generator;
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
}
