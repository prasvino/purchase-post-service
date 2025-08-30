package com.app.config;

import com.app.media.worker.AzureBlobEventListener;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.storage.connection-string}")
    private String storageConnectionString;

    @Value("${azure.servicebus.connection-string}")
    private String serviceBusConnectionString;

    @Value("${azure.servicebus.queue-name}")
    private String queueName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient();
    }

    @Bean
    public ServiceBusProcessorClient serviceBusProcessorClient(
            AzureBlobEventListener azureBlobEventListener) {
        return new ServiceBusClientBuilder()
                .connectionString(serviceBusConnectionString)
                .processor()
                .queueName(queueName)
                .processMessage(context -> {
                    try {
                        azureBlobEventListener.handleBlobEvent(context.getMessage().getBody().toString());
                        context.complete();
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        context.abandon();
                    }
                })
                .processError(context -> {
                    System.err.println("Error occurred while processing message: " + context.getException().getMessage());
                })
                .buildProcessorClient();
    }
}