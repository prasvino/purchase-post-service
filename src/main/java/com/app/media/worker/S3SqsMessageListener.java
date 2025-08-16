package com.app.media.worker;

import lombok.RequiredArgsConstructor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class S3SqsMessageListener {
    private final S3EventListener s3EventListener;

    @SqsListener("${app.s3.sqsQueue}")
    public void receiveMessage(Message<String> message) {
        s3EventListener.handleS3Event(message);
    }
}
