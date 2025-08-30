package com.app.media.service;

import com.app.common.exception.NotFoundException;
import com.app.media.azure.AzureSasUrlGenerator;
import com.app.media.dto.PresignedUrlRequest;
import com.app.media.dto.PresignedUrlResponse;
import com.app.media.entity.Media;
import com.app.media.repo.MediaRepository;
import com.app.user.entity.User;
import com.app.user.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private AzureSasUrlGenerator generator;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private User testUser;
    private Media testMedia;
    private PresignedUrlRequest presignedUrlRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .build();

        testMedia = Media.builder()
                .id(UUID.randomUUID())
                .uploader(testUser)
                .fileName("test-image.jpg")
                .fileType("image/jpeg")
                .size(1024L)
                .status("UPLOADED")
                .createdAt(Instant.now())
                .build();

        presignedUrlRequest = new PresignedUrlRequest();
        presignedUrlRequest.setFileName("test-image.jpg");
        presignedUrlRequest.setFileType("image/jpeg");
        presignedUrlRequest.setSize(1024L);
    }

    @Test
    void createPresignedUpload_ValidRequest_ReturnsPresignedUrlResponse() {
        // Given
        String expectedUploadUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-test-image.jpg?sv=2023-11-03&sr=c&sig=test";
        String expectedFileUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-test-image.jpg";

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn(expectedUploadUrl);
        when(generator.getPublicFileUrl(anyString())).thenReturn(expectedFileUrl);

        // When
        PresignedUrlResponse result = mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUploadUrl()).isEqualTo(expectedUploadUrl);
        assertThat(result.getFileUrl()).isEqualTo(expectedFileUrl);
        assertThat(result.getMediaId()).isEqualTo(testMedia.getId());

        // Verify media repository is called twice (first save and then update with URL)
        verify(mediaRepository, times(2)).save(any(Media.class));
    }

    @Test
    void createPresignedUpload_NoUsersInDB_ThrowsNotFoundException() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> mediaService.createPresignedUpload(presignedUrlRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No user in DB");

        // Verify no media operations were performed
        verifyNoInteractions(mediaRepository);
        verifyNoInteractions(generator);
    }

    @Test
    void createPresignedUpload_CreatesMediaWithCorrectData() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
        when(mediaRepository.save(mediaCaptor.capture())).thenReturn(testMedia);

        // When
        mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        Media capturedMedia = mediaCaptor.getAllValues().get(0); // First save call
        assertThat(capturedMedia.getUploader()).isEqualTo(testUser);
        assertThat(capturedMedia.getFileName()).isEqualTo("test-image.jpg");
        assertThat(capturedMedia.getFileType()).isEqualTo("image/jpeg");
        assertThat(capturedMedia.getSize()).isEqualTo(1024L);
        assertThat(capturedMedia.getStatus()).isEqualTo("UPLOADED");
        assertThat(capturedMedia.getCreatedAt()).isNotNull();
    }

    @Test
    void createPresignedUpload_UpdatesMediaWithUrl() {
        // Given
        String expectedFileUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-test-image.jpg";
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn(expectedFileUrl);

        ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
        when(mediaRepository.save(mediaCaptor.capture())).thenReturn(testMedia);

        // When
        mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        // Check the second save call (after URL is set)
        assertThat(mediaCaptor.getAllValues()).hasSize(2);
        Media updatedMedia = mediaCaptor.getAllValues().get(1);
        assertThat(updatedMedia.getUrl()).isEqualTo(expectedFileUrl);
    }

    @Test
    void createPresignedUpload_GeneratesCorrectAzureBlobKey() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(generator).generateUploadUrl(keyCaptor.capture(), eq("image/jpeg"));
        
        String generatedKey = keyCaptor.getValue();
        assertThat(generatedKey).startsWith("uploads/" + testUser.getId() + "/");
        assertThat(generatedKey).contains(testMedia.getId().toString());
        assertThat(generatedKey).endsWith("test-image.jpg");
    }

    @Test
    void createPresignedUpload_VideoFile_HandlesCorrectly() {
        // Given
        presignedUrlRequest.setFileName("video.mp4");
        presignedUrlRequest.setFileType("video/mp4");
        presignedUrlRequest.setSize(10485760L); // 10MB

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        PresignedUrlResponse result = mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        assertThat(result).isNotNull();
        verify(generator).generateUploadUrl(anyString(), eq("video/mp4"));
    }

    @Test
    void createPresignedUpload_DocumentFile_HandlesCorrectly() {
        // Given
        presignedUrlRequest.setFileName("document.pdf");
        presignedUrlRequest.setFileType("application/pdf");
        presignedUrlRequest.setSize(5242880L); // 5MB

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        PresignedUrlResponse result = mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        assertThat(result).isNotNull();
        verify(generator).generateUploadUrl(anyString(), eq("application/pdf"));
    }

    @Test
    void createPresignedUpload_LargeFile_HandlesCorrectly() {
        // Given
        presignedUrlRequest.setFileName("large-file.zip");
        presignedUrlRequest.setFileType("application/zip");
        presignedUrlRequest.setSize(104857600L); // 100MB

        // Create a media object with the correct size for this test
        Media largeFileMedia = Media.builder()
                .id(UUID.randomUUID())
                .uploader(testUser)
                .fileName("large-file.zip")
                .fileType("application/zip")
                .size(104857600L) // 100MB
                .status("UPLOADED")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(largeFileMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        PresignedUrlResponse result = mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        assertThat(result).isNotNull();
        verify(generator).generateUploadUrl(anyString(), eq("application/zip"));
        
        // Verify the media entity has the correct size
        ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository, atLeastOnce()).save(mediaCaptor.capture());
        assertThat(mediaCaptor.getValue().getSize()).isEqualTo(104857600L);
    }

    @Test
    void createPresignedUpload_FileWithSpecialCharacters_HandlesCorrectly() {
        // Given
        presignedUrlRequest.setFileName("file name with spaces & symbols.jpg");
        presignedUrlRequest.setFileType("image/jpeg");
        presignedUrlRequest.setSize(2048L);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        PresignedUrlResponse result = mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        assertThat(result).isNotNull();
        
        // Verify the generated key contains the original filename
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(generator).generateUploadUrl(keyCaptor.capture(), eq("image/jpeg"));
        assertThat(keyCaptor.getValue()).endsWith("file name with spaces & symbols.jpg");
    }

    @Test
    void createPresignedUpload_CallsGeneratorWithCorrectParameters() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(generator).generateUploadUrl(keyCaptor.capture(), eq("image/jpeg"));
        verify(generator).getPublicFileUrl(keyCaptor.getValue());
        
        // Verify the same key is used for both operations
        String capturedKey = keyCaptor.getValue();
        assertThat(capturedKey).isNotNull();
        assertThat(capturedKey).isNotEmpty();
    }

    @Test
    void createPresignedUpload_SuccessfulTransaction_SavesMediaTwice() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
        when(generator.generateUploadUrl(anyString(), anyString())).thenReturn("upload-url");
        when(generator.getPublicFileUrl(anyString())).thenReturn("file-url");

        // When
        mediaService.createPresignedUpload(presignedUrlRequest);

        // Then
        verify(mediaRepository, times(2)).save(any(Media.class));
        verify(generator, times(1)).generateUploadUrl(anyString(), anyString());
        verify(generator, times(1)).getPublicFileUrl(anyString());
    }
}