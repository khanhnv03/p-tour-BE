-- password reset tokens
CREATE TABLE password_reset_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME     NOT NULL,
    used       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- per-user notification preferences (created on first access)
CREATE TABLE user_notification_preferences (
    id                  BIGINT     AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL UNIQUE,
    booking_alerts      TINYINT(1) NOT NULL DEFAULT 1,
    editorial_comments  TINYINT(1) NOT NULL DEFAULT 1,
    system_status       TINYINT(1) NOT NULL DEFAULT 0,
    created_at          DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_unp_user FOREIGN KEY (user_id) REFERENCES users (id)
);
