CREATE TABLE blog_block_images (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    blog_block_id   BIGINT UNSIGNED NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    alt_text        VARCHAR(255),
    sort_order      SMALLINT        NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_blog_block_images_block FOREIGN KEY (blog_block_id) REFERENCES blog_blocks(id) ON DELETE CASCADE,
    INDEX idx_blog_block_images_block (blog_block_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE media_assets (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    original_filename   VARCHAR(255)    NOT NULL,
    stored_filename     VARCHAR(255)    NOT NULL UNIQUE,
    url                 VARCHAR(500)    NOT NULL,
    alt_text            VARCHAR(255),
    content_type        VARCHAR(100)    NOT NULL,
    size                BIGINT          NOT NULL,
    uploaded_by         BIGINT UNSIGNED,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    CONSTRAINT fk_media_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_media_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
