-- Fix column type mismatches for Double fields
ALTER TABLE purchase_service.users ALTER COLUMN avg_rating TYPE DOUBLE PRECISION;
ALTER TABLE purchase_service.users ALTER COLUMN total_spent TYPE DOUBLE PRECISION;