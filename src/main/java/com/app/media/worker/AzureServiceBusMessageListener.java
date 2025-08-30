package com.app.media.worker;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
@RequiredArgsConstructor
@Slf4j
public class AzureServiceBusMessageListener {
    
    private final ServiceBusProcessorClient serviceBusProcessorClient;
    private final AzureBlobEventListener azureBlobEventListener;

    @PostConstruct
    public void startProcessing() {
        log.info("Starting Azure Service Bus message processing...");
        serviceBusProcessorClient.start();
    }

    @PreDestroy
    public void stopProcessing() {
        log.info("Stopping Azure Service Bus message processing...");
        serviceBusProcessorClient.close();
    }

    // This method will be called by the ServiceBusProcessorClient configured in AzureConfig
    private void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        log.info("Received Azure Service Bus message: {}", message.getBody().toString());
        
        try {
            // Process the blob event
            azureBlobEventListener.handleBlobEvent(message.getBody().toString());
            
            // Complete the message to remove it from the queue
            context.complete();
            log.info("Successfully processed and completed message");
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            // You might want to abandon or dead-letter the message here
            context.abandon();
        }
    }

    private void processError(com.azure.messaging.servicebus.ServiceBusErrorContext context) {
        log.error("Error occurred while processing Azure Service Bus message: {}", 
                context.getException().getMessage(), context.getException());
    }
}