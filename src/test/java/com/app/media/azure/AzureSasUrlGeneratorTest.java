package com.app.media.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureSasUrlGeneratorTest {

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @InjectMocks
    private AzureSasUrlGenerator azureSasUrlGenerator;

    private static final String TEST_CONTAINER = "purchase-post-media";
    private static final String TEST_ACCOUNT_NAME = "teststorageaccount";
    private static final String TEST_BLOB_NAME = "uploads/user-id/media-id-test-image.jpg";
    private static final String TEST_CONTENT_TYPE = "image/jpeg";

    @BeforeEach
    void setUp() {
        // Set the container name and account name using reflection since they're injected via @Value
        ReflectionTestUtils.setField(azureSasUrlGenerator, "containerName", TEST_CONTAINER);
        ReflectionTestUtils.setField(azureSasUrlGenerator, "accountName", TEST_ACCOUNT_NAME);
    }

    @Test
    void generateUploadUrl_ValidParameters_ReturnsSasUrl() {
        // Given
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        String expectedUrl = String.format("https://%s.blob.core.windows.net/%s/%s?%s", 
                TEST_ACCOUNT_NAME, TEST_CONTAINER, TEST_BLOB_NAME, expectedSasToken);

        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        String result = azureSasUrlGenerator.generateUploadUrl(TEST_BLOB_NAME, TEST_CONTENT_TYPE);

        // Then
        assertThat(result).isEqualTo(expectedUrl);
        verify(blobServiceClient).getBlobContainerClient(TEST_CONTAINER);
        verify(blobContainerClient).exists();
        verify(blobContainerClient).generateSas(any(BlobServiceSasSignatureValues.class));
    }

    @Test
    void generateUploadUrl_ContainerDoesNotExist_CreatesContainer() {
        // Given
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        
        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(false);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        azureSasUrlGenerator.generateUploadUrl(TEST_BLOB_NAME, TEST_CONTENT_TYPE);

        // Then
        verify(blobContainerClient).exists();
        verify(blobContainerClient).create();
        verify(blobContainerClient).generateSas(any(BlobServiceSasSignatureValues.class));
    }

    @Test
    void generateUploadUrl_ImageFile_HandlesCorrectly() {
        // Given
        String imageBlobName = "uploads/user-123/media-456-profile.png";
        String imageContentType = "image/png";
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        
        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        String result = azureSasUrlGenerator.generateUploadUrl(imageBlobName, imageContentType);

        // Then
        assertThat(result).contains(imageBlobName);
        assertThat(result).contains(expectedSasToken);
        
        // Verify SAS signature was generated with correct parameters
        ArgumentCaptor<BlobServiceSasSignatureValues> captor = ArgumentCaptor.forClass(BlobServiceSasSignatureValues.class);
        verify(blobContainerClient).generateSas(captor.capture());
        
        BlobServiceSasSignatureValues capturedValues = captor.getValue();
        assertThat(capturedValues.getContentType()).isEqualTo(imageContentType);
    }

    @Test
    void generateUploadUrl_VideoFile_HandlesCorrectly() {
        // Given
        String videoBlobName = "uploads/user-789/media-101-demo.mp4";
        String videoContentType = "video/mp4";
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        
        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        String result = azureSasUrlGenerator.generateUploadUrl(videoBlobName, videoContentType);

        // Then
        assertThat(result).contains(videoBlobName);
        assertThat(result).contains(expectedSasToken);
        
        // Verify SAS signature was generated with correct content type
        ArgumentCaptor<BlobServiceSasSignatureValues> captor = ArgumentCaptor.forClass(BlobServiceSasSignatureValues.class);
        verify(blobContainerClient).generateSas(captor.capture());
        
        BlobServiceSasSignatureValues capturedValues = captor.getValue();
        assertThat(capturedValues.getContentType()).isEqualTo(videoContentType);
    }

    @Test
    void generateUploadUrl_DocumentFile_HandlesCorrectly() {
        // Given
        String docBlobName = "uploads/user-456/media-789-document.pdf";
        String docContentType = "application/pdf";
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        
        when(blobServiceClient.getBlobContainerClient(TEST_CONTAINER)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        String result = azureSasUrlGenerator.generateUploadUrl(docBlobName, docContentType);

        // Then
        assertThat(result).contains(docBlobName);
        assertThat(result).contains(expectedSasToken);
        
        // Verify SAS signature was generated with correct content type
        ArgumentCaptor<BlobServiceSasSignatureValues> captor = ArgumentCaptor.forClass(BlobServiceSasSignatureValues.class);
        verify(blobContainerClient).generateSas(captor.capture());
        
        BlobServiceSasSignatureValues capturedValues = captor.getValue();
        assertThat(capturedValues.getContentType()).isEqualTo(docContentType);
    }

    @Test
    void getPublicFileUrl_ValidBlobName_ReturnsCorrectUrl() {
        // Given
        String blobName = "uploads/user-id/media-id-test-image.jpg";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(blobName);

        // Then
        String expectedUrl = String.format("https://%s.blob.core.windows.net/%s/%s", 
                TEST_ACCOUNT_NAME, TEST_CONTAINER, blobName);
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_ImageFile_ReturnsCorrectUrl() {
        // Given
        String imageBlobName = "uploads/user-123/media-456-profile.png";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(imageBlobName);

        // Then
        String expectedUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-123/media-456-profile.png";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_VideoFile_ReturnsCorrectUrl() {
        // Given
        String videoBlobName = "uploads/user-789/media-101-demo.mp4";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(videoBlobName);

        // Then
        String expectedUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-789/media-101-demo.mp4";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_DocumentFile_ReturnsCorrectUrl() {
        // Given
        String docBlobName = "uploads/user-456/media-789-document.pdf";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(docBlobName);

        // Then
        String expectedUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-456/media-789-document.pdf";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_BlobNameWithSpecialCharacters_ReturnsCorrectUrl() {
        // Given
        String specialBlobName = "uploads/user-id/media-id-file name with spaces.jpg";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(specialBlobName);

        // Then
        String expectedUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/user-id/media-id-file name with spaces.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getPublicFileUrl_DeepPath_ReturnsCorrectUrl() {
        // Given
        String deepBlobName = "uploads/2023/12/user-id/media-id/thumbnail/image.jpg";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(deepBlobName);

        // Then
        String expectedUrl = "https://teststorageaccount.blob.core.windows.net/purchase-post-media/uploads/2023/12/user-id/media-id/thumbnail/image.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void generateUploadUrl_DifferentContainerName_UsesCorrectContainer() {
        // Given
        String differentContainer = "another-container";
        ReflectionTestUtils.setField(azureSasUrlGenerator, "containerName", differentContainer);
        
        String expectedSasToken = "sv=2023-11-03&sr=c&sp=wc&se=2024-01-01T00%3A00%3A00Z&sig=test-signature";
        
        when(blobServiceClient.getBlobContainerClient(differentContainer)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.generateSas(any(BlobServiceSasSignatureValues.class))).thenReturn(expectedSasToken);

        // When
        azureSasUrlGenerator.generateUploadUrl(TEST_BLOB_NAME, TEST_CONTENT_TYPE);

        // Then
        verify(blobServiceClient).getBlobContainerClient(differentContainer);
    }

    @Test
    void getPublicFileUrl_DifferentAccountName_ReturnsCorrectUrl() {
        // Given
        String differentAccountName = "anotherstorageaccount";
        ReflectionTestUtils.setField(azureSasUrlGenerator, "accountName", differentAccountName);
        String blobName = "uploads/test-file.jpg";

        // When
        String result = azureSasUrlGenerator.getPublicFileUrl(blobName);

        // Then
        String expectedUrl = "https://anotherstorageaccount.blob.core.windows.net/purchase-post-media/uploads/test-file.jpg";
        assertThat(result).isEqualTo(expectedUrl);
    }
}