-- Create post_media join table for Many-to-Many relationship
CREATE TABLE purchase_service.post_media (
    post_id UUID NOT NULL,
    media_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, media_id),
    FOREIGN KEY (post_id) REFERENCES purchase_service.posts(id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES purchase_service.media(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_post_media_post_id ON purchase_service.post_media(post_id);
CREATE INDEX idx_post_media_media_id ON purchase_service.post_media(media_id);