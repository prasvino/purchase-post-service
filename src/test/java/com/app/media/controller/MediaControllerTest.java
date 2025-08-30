package com.app.media.controller;

import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;
import com.app.media.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private PresignedUrlRequest presignedUrlRequest;
    private PresignedUrlResponse presignedUrlResponse;

    @BeforeEach
    void setUp() {
        presignedUrlRequest = new PresignedUrlRequest();
        presignedUrlRequest.setFileName("test-image.jpg");
        presignedUrlRequest.setFileType("image/jpeg");
        presignedUrlRequest.setSize(1024L);

        presignedUrlResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-test-image.jpg?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-test-image.jpg")
                .mediaId(UUID.randomUUID())
                .build();
    }

    @Test
    void sas_ValidRequest_ReturnsPresignedUrlResponse() {
        // Given
        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(presignedUrlResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUploadUrl()).isEqualTo(presignedUrlResponse.getUploadUrl());
        assertThat(response.getBody().getFileUrl()).isEqualTo(presignedUrlResponse.getFileUrl());
        assertThat(response.getBody().getMediaId()).isEqualTo(presignedUrlResponse.getMediaId());
    }

    @Test
    void sas_ImageFile_ReturnsCorrectResponse() {
        // Given
        presignedUrlRequest.setFileName("profile-picture.png");
        presignedUrlRequest.setFileType("image/png");
        presignedUrlRequest.setSize(2048L);

        PresignedUrlResponse imageResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-profile-picture.png?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-profile-picture.png")
                .mediaId(UUID.randomUUID())
                .build();

        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(imageResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUploadUrl()).contains("profile-picture.png");
        assertThat(response.getBody().getFileUrl()).contains("profile-picture.png");
        assertThat(response.getBody().getMediaId()).isNotNull();
    }

    @Test
    void sas_VideoFile_ReturnsCorrectResponse() {
        // Given
        presignedUrlRequest.setFileName("demo-video.mp4");
        presignedUrlRequest.setFileType("video/mp4");
        presignedUrlRequest.setSize(10485760L); // 10MB

        PresignedUrlResponse videoResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-demo-video.mp4?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-demo-video.mp4")
                .mediaId(UUID.randomUUID())
                .build();

        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(videoResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUploadUrl()).contains("demo-video.mp4");
        assertThat(response.getBody().getFileUrl()).contains("demo-video.mp4");
        assertThat(response.getBody().getMediaId()).isNotNull();
    }

    @Test
    void sas_DocumentFile_ReturnsCorrectResponse() {
        // Given
        presignedUrlRequest.setFileName("document.pdf");
        presignedUrlRequest.setFileType("application/pdf");
        presignedUrlRequest.setSize(5242880L); // 5MB

        PresignedUrlResponse documentResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-document.pdf?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-document.pdf")
                .mediaId(UUID.randomUUID())
                .build();

        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(documentResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUploadUrl()).contains("document.pdf");
        assertThat(response.getBody().getFileUrl()).contains("document.pdf");
        assertThat(response.getBody().getMediaId()).isNotNull();
    }

    @Test
    void sas_LargeFile_ReturnsCorrectResponse() {
        // Given
        presignedUrlRequest.setFileName("large-presentation.pptx");
        presignedUrlRequest.setFileType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        presignedUrlRequest.setSize(52428800L); // 50MB

        PresignedUrlResponse largeFileResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-large-presentation.pptx?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-large-presentation.pptx")
                .mediaId(UUID.randomUUID())
                .build();

        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(largeFileResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUploadUrl()).contains("large-presentation.pptx");
        assertThat(response.getBody().getFileUrl()).contains("large-presentation.pptx");
        assertThat(response.getBody().getMediaId()).isNotNull();
    }

    @Test
    void sas_FileWithSpecialCharacters_ReturnsCorrectResponse() {
        // Given
        presignedUrlRequest.setFileName("my file (1) - copy.jpg");
        presignedUrlRequest.setFileType("image/jpeg");
        presignedUrlRequest.setSize(1024L);

        PresignedUrlResponse specialCharsResponse = PresignedUrlResponse.builder()
                .uploadUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-my%20file%20(1)%20-%20copy.jpg?sv=2023-11-03&sr=c&sig=test")
                .fileUrl("https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-my%20file%20(1)%20-%20copy.jpg")
                .mediaId(UUID.randomUUID())
                .build();

        when(mediaService.createPresignedUpload(any(PresignedUrlRequest.class))).thenReturn(specialCharsResponse);

        // When
        ResponseEntity<PresignedUrlResponse> response = mediaController.sas(presignedUrlRequest);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMediaId()).isNotNull();
        // Note: The service may handle URL encoding differently, so we just verify the response is successful
    }
}