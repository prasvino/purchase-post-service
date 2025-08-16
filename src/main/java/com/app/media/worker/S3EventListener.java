package com.app.media.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.media.entity.Media;
import com.app.media.repo.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3EventListener {
    private final MediaRepository mediaRepository;
    private final ObjectMapper objectMapper;
    private final MediaProcessingService mediaProcessingService;

    @Async
    public void handleS3Event(Message<String> message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            for (JsonNode record : root.path("Records")) {
                String s3Key = record.path("s3").path("object").path("key").asText();
                log.info("Received S3 event for key: {}", s3Key);
                updateMediaStatusAndProcess(s3Key);
            }
        } catch (Exception e) {
            log.error("Error processing S3 event: {}", e.getMessage(), e);
        }
    }

    private void updateMediaStatusAndProcess(String s3Key) {
        try {
            String idPart = s3Key.split("/")[2].split("-")[0];
            UUID mediaId = UUID.fromString(idPart);
            Optional<Media> mediaOpt = mediaRepository.findById(mediaId);
            mediaOpt.ifPresent(media -> {
                media.setStatus("READY");
                mediaRepository.save(media);
                log.info("Marked media {} as READY", mediaId);
                if (media.getFileType().startsWith("image")) {
                    mediaProcessingService.generateThumbnail(media.getUrl());
                } else if (media.getFileType().startsWith("video")) {
                    mediaProcessingService.generateThumbnail(media.getUrl());
                    mediaProcessingService.transcodeVideo(media.getUrl());
                }
            });
        } catch (Exception e) {
            log.warn("Unable to parse mediaId from key: {}", s3Key);
        }
    }
}
