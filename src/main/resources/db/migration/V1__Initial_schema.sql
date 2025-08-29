-- Create dedicated schema for purchase service
CREATE SCHEMA IF NOT EXISTS purchase_service;

CREATE TABLE IF NOT EXISTS purchase_service.users (
  id UUID PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  display_name VARCHAR(200),
  email VARCHAR(200) UNIQUE,
  password VARCHAR(255),
  avatar_url TEXT,
  bio TEXT,
  location VARCHAR(200),
  website TEXT,
  joined_at TIMESTAMP,
  is_verified BOOLEAN DEFAULT FALSE,
  followers_count INT DEFAULT 0,
  following_count INT DEFAULT 0,
  posts_count INT DEFAULT 0,
  total_spent DECIMAL(10,2) DEFAULT 0.00,
  avg_rating DOUBLE PRECISION DEFAULT 0.00,
  is_online BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS purchase_service.platforms (
  id UUID PRIMARY KEY,
  name VARCHAR(200),
  domain VARCHAR(200),
  logo_url TEXT,
  verified BOOLEAN
);

CREATE TABLE IF NOT EXISTS purchase_service.media (
  id UUID PRIMARY KEY,
  uploader_id UUID,
  file_name VARCHAR(255),
  file_type VARCHAR(100),
  url TEXT,
  size BIGINT,
  status VARCHAR(50),
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_service.posts (
  id UUID PRIMARY KEY,
  author_id UUID,
  text TEXT,
  purchase_date DATE,
  price NUMERIC,
  currency VARCHAR(10),
  platform_id UUID,
  product_url TEXT,
  visibility VARCHAR(50),
  metadata TEXT,
  like_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  repost_count INT DEFAULT 0,
  share_count INT DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_service.comments (
  id UUID PRIMARY KEY,
  post_id UUID NOT NULL,
  author_id UUID NOT NULL,
  parent_comment_id UUID,
  text TEXT NOT NULL,
  like_count INT DEFAULT 0,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

-- Foreign key constraints (with safe handling for existing constraints)
-- Drop existing constraints if they exist
ALTER TABLE IF EXISTS purchase_service.posts DROP CONSTRAINT IF EXISTS fk_posts_platform;
ALTER TABLE IF EXISTS purchase_service.media DROP CONSTRAINT IF EXISTS fk_media_uploader;

-- Add constraints
ALTER TABLE purchase_service.posts ADD CONSTRAINT fk_posts_platform FOREIGN KEY (platform_id) REFERENCES purchase_service.platforms(id);
ALTER TABLE purchase_service.media ADD CONSTRAINT fk_media_uploader FOREIGN KEY (uploader_id) REFERENCES purchase_service.users(id);
