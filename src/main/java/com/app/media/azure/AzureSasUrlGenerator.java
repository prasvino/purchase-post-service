package com.app.media.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class AzureSasUrlGenerator {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.container}")
    private String containerName;

    @Value("${azure.storage.account-name}")
    private String accountName;

    public AzureSasUrlGenerator(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    public String generateUploadUrl(String blobName, String contentType) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        // Create the container if it doesn't exist
        if (!containerClient.exists()) {
            containerClient.create();
        }

        // Set permissions for the SAS token - allow write for upload
        BlobContainerSasPermission sasPermission = new BlobContainerSasPermission()
                .setWritePermission(true)
                .setCreatePermission(true);

        // Create SAS signature values
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(10), // Expires in 10 minutes
                sasPermission
        )
                .setProtocol(SasProtocol.HTTPS_ONLY)
                .setContentType(contentType);

        // Generate the SAS token
        String sasToken = containerClient.generateSas(sasSignatureValues);
        
        // Return the full URL with SAS token for the specific blob
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s", 
                accountName, containerName, blobName, sasToken);
    }

    public String getPublicFileUrl(String blobName) {
        return String.format("https://%s.blob.core.windows.net/%s/%s", 
                accountName, containerName, blobName);
    }
}