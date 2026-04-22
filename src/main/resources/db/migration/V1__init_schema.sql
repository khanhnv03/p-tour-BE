-- =============================================================
-- PTour – Initial Schema
-- Charset: utf8mb4 | Engine: InnoDB
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- 1. users
-- -------------------------------------------------------------
CREATE TABLE users (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password_hash       VARCHAR(255)    NOT NULL,
    full_name           VARCHAR(255)    NOT NULL,
    phone               VARCHAR(20),
    avatar_url          VARCHAR(500),
    address             TEXT,
    role                ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    status              ENUM('ACTIVE','BLOCKED')  NOT NULL DEFAULT 'ACTIVE',
    email_verified_at   DATETIME,
    remember_token      VARCHAR(100),
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 2. destinations
-- -------------------------------------------------------------
CREATE TABLE destinations (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    UNIQUE,
    description     TEXT,
    cover_image_url VARCHAR(500),
    country         VARCHAR(100)    NOT NULL DEFAULT 'Việt Nam',
    region          VARCHAR(100),
    is_featured     TINYINT(1)      NOT NULL DEFAULT 0,
    tour_count      INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 3. tours
-- -------------------------------------------------------------
CREATE TABLE tours (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    destination_id      BIGINT UNSIGNED NOT NULL,
    title               VARCHAR(500)    NOT NULL,
    slug                VARCHAR(500)    UNIQUE,
    description         TEXT,
    duration_days       TINYINT UNSIGNED NOT NULL,
    duration_nights     TINYINT UNSIGNED NOT NULL,
    max_guests          TINYINT UNSIGNED NOT NULL,
    difficulty          ENUM('EASY','MEDIUM','HARD') NOT NULL,
    price_per_person    DECIMAL(15,2)   NOT NULL,
    cover_image_url     VARCHAR(500),
    status              ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    rating              DECIMAL(2,1)    NOT NULL DEFAULT 0.0,
    review_count        INT             NOT NULL DEFAULT 0,
    booking_count       INT             NOT NULL DEFAULT 0,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    CONSTRAINT fk_tours_destination FOREIGN KEY (destination_id) REFERENCES destinations(id),
    INDEX idx_tours_destination (destination_id),
    INDEX idx_tours_status      (status),
    INDEX idx_tours_price       (price_per_person)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 4. tour_gallery_images
-- -------------------------------------------------------------
CREATE TABLE tour_gallery_images (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id     BIGINT UNSIGNED NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    CONSTRAINT fk_gallery_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 5. tour_highlights
-- -------------------------------------------------------------
CREATE TABLE tour_highlights (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id     BIGINT UNSIGNED NOT NULL,
    icon        VARCHAR(100)    NOT NULL,
    label       VARCHAR(255)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    CONSTRAINT fk_highlights_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 6. tour_inclusions
-- -------------------------------------------------------------
CREATE TABLE tour_inclusions (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id     BIGINT UNSIGNED NOT NULL,
    type        ENUM('INCLUDE','EXCLUDE') NOT NULL,
    description VARCHAR(500)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    CONSTRAINT fk_inclusions_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 7. itinerary_days
-- -------------------------------------------------------------
CREATE TABLE itinerary_days (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id         BIGINT UNSIGNED NOT NULL,
    day_number      TINYINT UNSIGNED NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    summary         TEXT,
    cover_image_url VARCHAR(500),
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_itinerary_days_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    UNIQUE KEY uk_itinerary_tour_day (tour_id, day_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 8. itinerary_activities
-- -------------------------------------------------------------
CREATE TABLE itinerary_activities (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    itinerary_day_id    BIGINT UNSIGNED NOT NULL,
    activity_time       TIME            NOT NULL,
    description         TEXT            NOT NULL,
    sort_order          TINYINT         NOT NULL DEFAULT 0,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    CONSTRAINT fk_activities_day FOREIGN KEY (itinerary_day_id) REFERENCES itinerary_days(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 9. tour_departures
-- -------------------------------------------------------------
CREATE TABLE tour_departures (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id         BIGINT UNSIGNED NOT NULL,
    departure_date  DATE            NOT NULL,
    available_slots TINYINT UNSIGNED NOT NULL,
    booked_slots    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    price_override  DECIMAL(15,2),
    status          ENUM('OPEN','FULL','CANCELLED') NOT NULL DEFAULT 'OPEN',
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_departures_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    INDEX idx_departures_date (departure_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 10. deals
-- -------------------------------------------------------------
CREATE TABLE deals (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(500)    NOT NULL,
    description         TEXT,
    campaign_image_url  VARCHAR(500),
    badge_text          VARCHAR(100),
    category            VARCHAR(100),
    discount_type       ENUM('FIXED','PERCENTAGE') NOT NULL,
    discount_value      DECIMAL(15,2)   NOT NULL,
    promo_code          VARCHAR(50)     UNIQUE,
    display_mode        ENUM('COPY_CODE','AUTO_APPLY') NOT NULL,
    min_order_value     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(15,2),
    usage_limit         INT,
    usage_count         INT             NOT NULL DEFAULT 0,
    valid_from          DATE            NOT NULL,
    valid_to            DATE            NOT NULL,
    status              ENUM('ACTIVE','EXPIRED','DRAFT') NOT NULL DEFAULT 'DRAFT',
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    INDEX idx_deals_status (status),
    INDEX idx_deals_dates  (valid_from, valid_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 11. bookings
-- -------------------------------------------------------------
CREATE TABLE bookings (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_code    VARCHAR(20)     NOT NULL UNIQUE,
    user_id         BIGINT UNSIGNED NOT NULL,
    tour_id         BIGINT UNSIGNED NOT NULL,
    departure_id    BIGINT UNSIGNED NOT NULL,
    deal_id         BIGINT UNSIGNED,
    guest_count     TINYINT UNSIGNED NOT NULL,
    subtotal        DECIMAL(15,2)   NOT NULL,
    tax_amount      DECIMAL(15,2)   NOT NULL,
    discount_amount DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_amount    DECIMAL(15,2)   NOT NULL,
    status          ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_bookings_user      FOREIGN KEY (user_id)      REFERENCES users(id),
    CONSTRAINT fk_bookings_tour      FOREIGN KEY (tour_id)      REFERENCES tours(id),
    CONSTRAINT fk_bookings_departure FOREIGN KEY (departure_id) REFERENCES tour_departures(id),
    CONSTRAINT fk_bookings_deal      FOREIGN KEY (deal_id)      REFERENCES deals(id),
    INDEX idx_bookings_user   (user_id),
    INDEX idx_bookings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 12. orders
-- -------------------------------------------------------------
CREATE TABLE orders (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_code      VARCHAR(20)     NOT NULL UNIQUE,
    booking_id      BIGINT UNSIGNED NOT NULL UNIQUE,
    user_id         BIGINT UNSIGNED NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    payment_method  ENUM('CREDIT_CARD','BANK_TRANSFER','MOMO','VNPAY') NOT NULL,
    payment_status  ENUM('PENDING','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(255),
    card_last_four  CHAR(4),
    paid_at         DATETIME,
    refunded_at     DATETIME,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_orders_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_orders_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    INDEX idx_orders_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 13. reviews
-- -------------------------------------------------------------
CREATE TABLE reviews (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tour_id     BIGINT UNSIGNED NOT NULL,
    user_id     BIGINT UNSIGNED NOT NULL,
    booking_id  BIGINT UNSIGNED NOT NULL UNIQUE,
    rating      TINYINT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    is_verified TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    CONSTRAINT fk_reviews_tour    FOREIGN KEY (tour_id)    REFERENCES tours(id),
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_reviews_tour (tour_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 14. wishlists
-- -------------------------------------------------------------
CREATE TABLE wishlists (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    tour_id     BIGINT UNSIGNED NOT NULL,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    UNIQUE KEY uk_wishlist_user_tour (user_id, tour_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 15. blog_posts
-- -------------------------------------------------------------
CREATE TABLE blog_posts (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    author_id       BIGINT UNSIGNED NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    slug            VARCHAR(500)    UNIQUE,
    cover_image_url VARCHAR(500),
    excerpt         TEXT,
    status          ENUM('DRAFT','PUBLISHED','SCHEDULED') NOT NULL DEFAULT 'DRAFT',
    published_at    DATETIME,
    scheduled_at    DATETIME,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_blog_posts_author FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_blog_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- 16. blog_blocks
-- -------------------------------------------------------------
CREATE TABLE blog_blocks (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    blog_post_id    BIGINT UNSIGNED NOT NULL,
    block_type      ENUM('PARAGRAPH','HEADING','QUOTE','IMAGE','GALLERY') NOT NULL,
    content         TEXT,
    image_url       VARCHAR(500),
    sort_order      SMALLINT        NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    CONSTRAINT fk_blog_blocks_post FOREIGN KEY (blog_post_id) REFERENCES blog_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
