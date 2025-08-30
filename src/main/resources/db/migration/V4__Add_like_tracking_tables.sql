-- Add proper like tracking tables for posts and comments

-- Create post_likes table to track which users liked which posts
CREATE TABLE IF NOT EXISTS purchase_service.post_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, post_id)
);

-- Create comment_likes table to track which users liked which comments
CREATE TABLE IF NOT EXISTS purchase_service.comment_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    comment_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, comment_id)
);

-- Add foreign key constraints for post_likes
ALTER TABLE purchase_service.post_likes 
ADD CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES purchase_service.users(id) ON DELETE CASCADE;

ALTER TABLE purchase_service.post_likes 
ADD CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES purchase_service.posts(id) ON DELETE CASCADE;

-- Add foreign key constraints for comment_likes
ALTER TABLE purchase_service.comment_likes 
ADD CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES purchase_service.users(id) ON DELETE CASCADE;

ALTER TABLE purchase_service.comment_likes 
ADD CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES purchase_service.comments(id) ON DELETE CASCADE;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_post_likes_user_id ON purchase_service.post_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_post_id ON purchase_service.post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_comment_likes_user_id ON purchase_service.comment_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_comment_likes_comment_id ON purchase_service.comment_likes(comment_id);