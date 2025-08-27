package com.app.media.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3PresignedUrlGeneratorTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @InjectMocks
    private S3PresignedUrlGenerator s3PresignedUrlGenerator;

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_KEY = "uploads/user-id/media-id-test-image.jpg";
    private static final String TEST_CONTENT_TYPE = "image/jpeg";

    @BeforeEach
    void setUp() {
        // Set the bucket name using reflection since it's injected via @Value
        ReflectionTestUtils.setField(s3PresignedUrlGenerator, "bucketName", TEST_BUCKET);
    }

    @Test
    void generateUploadUrl_ValidParameters_ReturnsPresignedUrl() throws MalformedURLException {
        // Given
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/user-id/media-id-test-image.jpg?X-Amz-Signature=test";
        URL mockUrl = new URL(expectedUrl);
        
        when(presignedPutObjectRequest.url()).thenReturn(mockUrl);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

        // When
        String result = s3PresignedUrlGenerator.generateUploadUrl(TEST_KEY, TEST_CONTENT_TYPE);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void generateUploadUrl_VerifyPutObjectRequestParameters() {
        // Given
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(createMockUrl());

        // When
        s3PresignedUrlGenerator.generateUploadUrl(TEST_KEY, TEST_CONTENT_TYPE);

        // Then
        ArgumentCaptor<PutObjectPresignRequest> presignRequestCaptor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(presignRequestCaptor.capture());

        PutObjectPresignRequest capturedRequest = presignRequestCaptor.getValue();
        PutObjectRequest putObjectRequest = capturedRequest.putObjectRequest();
        
        assertThat(putObjectRequest.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(putObjectRequest.key()).isEqualTo(TEST_KEY);
        assertThat(putObjectRequest.contentType()).isEqualTo(TEST_CONTENT_TYPE);
        assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void generateUploadUrl_ImageFile_HandlesCorrectly() throws MalformedURLException {
        // Given
        String imageKey = "uploads/user-123/media-456-profile.png";
        String imageContentType = "image/png";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/" + imageKey + "?X-Amz-Signature=test";
        
        when(presignedPutObjectRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

        // When
        String result = s3PresignedUrlGenerator.generateUploadUrl(imageKey, imageContentType);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        
        // Verify correct parameters were used
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo(imageContentType);
    }

    @Test
    void generateUploadUrl_VideoFile_HandlesCorrectly() throws MalformedURLException {
        // Given
        String videoKey = "uploads/user-789/media-101-demo.mp4";
        String videoContentType = "video/mp4";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/" + videoKey + "?X-Amz-Signature=test";
        
        when(presignedPutObjectRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

        // When
        String result = s3PresignedUrlGenerator.generateUploadUrl(videoKey, videoContentType);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        
        // Verify correct parameters were used
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo(videoContentType);
    }

    @Test
    void generateUploadUrl_DocumentFile_HandlesCorrectly() throws MalformedURLException {
        // Given
        String docKey = "uploads/user-456/media-789-document.pdf";
        String docContentType = "application/pdf";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/" + docKey + "?X-Amz-Signature=test";
        
        when(presignedPutObjectRequest.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

        // When
        String result = s3PresignedUrlGenerator.generateUploadUrl(docKey, docContentType);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        
        // Verify correct parameters were used
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo(docContentType);
    }

    @Test
    void getPublicFileUrl_ValidKey_ReturnsCorrectUrl() {
        // Given
        String key = "uploads/user-id/media-id-test-image.jpg";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(key);

        // Then
        String expectedUrl = String.format("https://%s.s3.amazonaws.com/%s", TEST_BUCKET, key);
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_ImageFile_ReturnsCorrectUrl() {
        // Given
        String imageKey = "uploads/user-123/media-456-profile.png";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(imageKey);

        // Then
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/user-123/media-456-profile.png";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_VideoFile_ReturnsCorrectUrl() {
        // Given
        String videoKey = "uploads/user-789/media-101-demo.mp4";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(videoKey);

        // Then
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/user-789/media-101-demo.mp4";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_DocumentFile_ReturnsCorrectUrl() {
        // Given
        String docKey = "uploads/user-456/media-789-document.pdf";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(docKey);

        // Then
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/user-456/media-789-document.pdf";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_KeyWithSpecialCharacters_ReturnsCorrectUrl() {
        // Given
        String specialKey = "uploads/user-id/media-id-file name with spaces.jpg";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(specialKey);

        // Then
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/user-id/media-id-file name with spaces.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_DeepPath_ReturnsCorrectUrl() {
        // Given
        String deepKey = "uploads/2023/12/user-id/media-id/thumbnail/image.jpg";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(deepKey);

        // Then
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/uploads/2023/12/user-id/media-id/thumbnail/image.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void generateUploadUrl_VerifySignatureDuration() {
        // Given
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(createMockUrl());

        // When
        s3PresignedUrlGenerator.generateUploadUrl(TEST_KEY, TEST_CONTENT_TYPE);

        // Then
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        
        PutObjectPresignRequest request = captor.getValue();
        assertThat(request.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void generateUploadUrl_DifferentBucketName_UsesCorrectBucket() {
        // Given
        String differentBucket = "another-test-bucket";
        ReflectionTestUtils.setField(s3PresignedUrlGenerator, "bucketName", differentBucket);
        
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(createMockUrl());

        // When
        s3PresignedUrlGenerator.generateUploadUrl(TEST_KEY, TEST_CONTENT_TYPE);

        // Then
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        
        PutObjectRequest putObjectRequest = captor.getValue().putObjectRequest();
        assertThat(putObjectRequest.bucket()).isEqualTo(differentBucket);
    }

    @Test
    void getPublicFileUrl_DifferentBucketName_ReturnsCorrectUrl() {
        // Given
        String differentBucket = "another-test-bucket";
        ReflectionTestUtils.setField(s3PresignedUrlGenerator, "bucketName", differentBucket);
        String key = "uploads/test-file.jpg";

        // When
        String result = s3PresignedUrlGenerator.getPublicFileUrl(key);

        // Then
        String expectedUrl = "https://another-test-bucket.s3.amazonaws.com/uploads/test-file.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }

    private URL createMockUrl() {
        try {
            return new URL("https://test-bucket.s3.amazonaws.com/test-key?X-Amz-Signature=test");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}