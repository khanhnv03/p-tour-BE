-- OAuth2 support: make password nullable and add provider fields
ALTER TABLE users MODIFY password_hash VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN provider VARCHAR(50) NULL AFTER remember_token;
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255) NULL AFTER provider;
CREATE INDEX idx_users_provider ON users(provider, provider_id);
