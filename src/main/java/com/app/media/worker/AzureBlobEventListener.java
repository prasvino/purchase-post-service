package com.app.media.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.media.entity.Media;
import com.app.media.repo.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureBlobEventListener {
    private final MediaRepository mediaRepository;
    private final ObjectMapper objectMapper;
    private final MediaProcessingService mediaProcessingService;

    @Async
    public void handleBlobEvent(String messageBody) {
        try {
            log.info("Processing Azure Event Grid message from Service Bus: {}", messageBody);
            JsonNode root = objectMapper.readTree(messageBody);
            
            // Handle Event Grid events that come through Service Bus
            // The message could be a single event or an array of events
            if (root.isArray()) {
                for (JsonNode event : root) {
                    processSingleEventGridEvent(event);
                }
            } else {
                // Single event - could be direct Event Grid format or wrapped
                processSingleEventGridEvent(root);
            }
        } catch (Exception e) {
            log.error("Error processing Azure Event Grid message: {}", e.getMessage(), e);
        }
    }

    private void processSingleEventGridEvent(JsonNode event) {
        try {
            String eventType = event.path("eventType").asText();
            
            // Check if this is a blob created event from Event Grid
            if ("Microsoft.Storage.BlobCreated".equals(eventType)) {
                String blobUrl = event.path("data").path("url").asText();
                log.info("Received Event Grid blob created event for URL: {}", blobUrl);
                updateMediaStatusAndProcess(blobUrl);
            } else {
                log.debug("Ignoring event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing single Event Grid event: {}", e.getMessage(), e);
        }
    }

    private void updateMediaStatusAndProcess(String blobUrl) {
        try {
            // Extract blob name from URL
            // Format: https://accountname.blob.core.windows.net/container/uploads/userId/mediaId-filename
            String[] urlParts = blobUrl.split("/");
            String blobName = urlParts[urlParts.length - 1]; // gets "mediaId-filename"
            
            // Extract mediaId from blob name
            String idPart = blobName.split("-")[0];
            UUID mediaId = UUID.fromString(idPart);
            
            Optional<Media> mediaOpt = mediaRepository.findById(mediaId);
            mediaOpt.ifPresent(media -> {
                media.setStatus("READY");
                mediaRepository.save(media);
                log.info("Marked media {} as READY", mediaId);
                
                // Trigger processing based on file type
                if (media.getFileType().startsWith("image")) {
                    mediaProcessingService.generateThumbnail(media.getUrl());
                } else if (media.getFileType().startsWith("video")) {
                    mediaProcessingService.generateThumbnail(media.getUrl());
                    mediaProcessingService.transcodeVideo(media.getUrl());
                }
            });
        } catch (Exception e) {
            log.warn("Unable to parse mediaId from blob URL: {}", blobUrl, e);
        }
    }
}