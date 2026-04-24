CREATE TABLE contact_messages (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(30),
    subject         VARCHAR(255)    NOT NULL,
    message         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'NEW',
    assignee_id     BIGINT UNSIGNED,
    admin_note      TEXT,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_contact_assignee FOREIGN KEY (assignee_id) REFERENCES users(id),
    INDEX idx_contact_status (status),
    INDEX idx_contact_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE newsletter_subscriptions (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    unsubscribed_at     DATETIME(6),
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    UNIQUE KEY idx_newsletter_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
