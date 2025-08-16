-- Basic schema (H2/Postgres compatible)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  username VARCHAR(100) UNIQUE,
  display_name VARCHAR(200),
  email VARCHAR(200),
  avatar_url TEXT
);

CREATE TABLE IF NOT EXISTS platforms (
  id UUID PRIMARY KEY,
  name VARCHAR(200),
  domain VARCHAR(200),
  logo_url TEXT,
  verified BOOLEAN
);

CREATE TABLE IF NOT EXISTS media (
  id UUID PRIMARY KEY,
  uploader_id UUID,
  file_name VARCHAR(255),
  file_type VARCHAR(100),
  url TEXT,
  size BIGINT,
  status VARCHAR(50),
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS posts (
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
  like_count INT,
  comment_count INT,
  repost_count INT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
