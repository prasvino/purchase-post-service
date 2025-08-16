package com.app.media.entity;

import com.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Media {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    private String fileName;
    private String fileType;
    private String url;        // public file URL (CDN or s3)
    private Long size;
    private String status;     // UPLOADED, PROCESSING, READY
    private Instant createdAt;
}
