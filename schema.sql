-- =============================================================
-- P-TOUR DATABASE SCHEMA
-- Dự án: The PTIT Tour - Nền tảng đặt tour du lịch
-- Database: MySQL 8.0+
-- Charset: utf8mb4 (hỗ trợ tiếng Việt đầy đủ)
-- =============================================================

CREATE DATABASE IF NOT EXISTS ptour
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ptour;

-- =============================================================
-- 1. USERS — Người dùng (khách hàng + admin)
-- =============================================================
CREATE TABLE users (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(255)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    avatar_url      VARCHAR(500)    NULL,
    address         TEXT            NULL,
    role            ENUM('customer', 'admin') NOT NULL DEFAULT 'customer',
    status          ENUM('active', 'blocked') NOT NULL DEFAULT 'active',
    email_verified_at TIMESTAMP     NULL,
    remember_token  VARCHAR(100)    NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 2. DESTINATIONS — Điểm đến
-- =============================================================
CREATE TABLE destinations (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    description     TEXT            NULL,
    cover_image_url VARCHAR(500)    NULL,
    country         VARCHAR(100)    NOT NULL DEFAULT 'Việt Nam',
    region          VARCHAR(100)    NULL,
    is_featured     BOOLEAN         NOT NULL DEFAULT FALSE,
    tour_count      INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 3. TOURS — Tour du lịch
-- =============================================================
CREATE TABLE tours (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    destination_id  INT UNSIGNED    NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    slug            VARCHAR(500)    NOT NULL UNIQUE,
    description     TEXT            NULL,
    duration_days   TINYINT UNSIGNED NOT NULL,
    duration_nights TINYINT UNSIGNED NOT NULL,
    max_guests      TINYINT UNSIGNED NOT NULL DEFAULT 12,
    difficulty      ENUM('easy', 'medium', 'hard') NOT NULL DEFAULT 'medium',
    price_per_person DECIMAL(15,2)  NOT NULL,
    cover_image_url VARCHAR(500)    NULL,
    status          ENUM('draft', 'published', 'archived') NOT NULL DEFAULT 'draft',
    rating          DECIMAL(2,1)    NOT NULL DEFAULT 0.0,
    review_count    INT UNSIGNED    NOT NULL DEFAULT 0,
    booking_count   INT UNSIGNED    NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tours_destination
        FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 4. TOUR_GALLERY_IMAGES — Ảnh gallery của tour (tối đa 4 ảnh bento)
-- =============================================================
CREATE TABLE tour_gallery_images (
    id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id     INT UNSIGNED    NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gallery_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 5. TOUR_HIGHLIGHTS — Điểm nổi bật của tour (icon + nhãn)
-- =============================================================
CREATE TABLE tour_highlights (
    id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id     INT UNSIGNED    NOT NULL,
    icon        VARCHAR(100)    NOT NULL,
    label       VARCHAR(255)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_highlights_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 6. TOUR_INCLUSIONS — Dịch vụ bao gồm / không bao gồm
-- =============================================================
CREATE TABLE tour_inclusions (
    id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id     INT UNSIGNED    NOT NULL,
    type        ENUM('include', 'exclude') NOT NULL,
    description VARCHAR(500)    NOT NULL,
    sort_order  TINYINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_inclusions_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 7. ITINERARY_DAYS — Lịch trình theo ngày
-- =============================================================
CREATE TABLE itinerary_days (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id         INT UNSIGNED    NOT NULL,
    day_number      TINYINT UNSIGNED NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    summary         TEXT            NULL,
    cover_image_url VARCHAR(500)    NULL,
    UNIQUE KEY uq_tour_day (tour_id, day_number),
    CONSTRAINT fk_days_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 8. ITINERARY_ACTIVITIES — Hoạt động trong từng ngày
-- =============================================================
CREATE TABLE itinerary_activities (
    id                  INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    itinerary_day_id    INT UNSIGNED    NOT NULL,
    activity_time       TIME            NOT NULL,
    description         TEXT            NOT NULL,
    sort_order          TINYINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_activities_day
        FOREIGN KEY (itinerary_day_id) REFERENCES itinerary_days(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 9. TOUR_DEPARTURES — Lịch khởi hành
-- =============================================================
CREATE TABLE tour_departures (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id         INT UNSIGNED    NOT NULL,
    departure_date  DATE            NOT NULL,
    available_slots TINYINT UNSIGNED NOT NULL,
    booked_slots    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    price_override  DECIMAL(15,2)   NULL COMMENT 'Ghi đè giá cho ngày cụ thể, NULL = dùng giá mặc định của tour',
    status          ENUM('open', 'full', 'cancelled') NOT NULL DEFAULT 'open',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_departures_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 10. DEALS — Chương trình khuyến mãi / mã giảm giá
-- =============================================================
CREATE TABLE deals (
    id                  INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(500)    NOT NULL,
    description         TEXT            NULL,
    campaign_image_url  VARCHAR(500)    NULL,
    badge_text          VARCHAR(100)    NULL,
    category            VARCHAR(100)    NULL,
    discount_type       ENUM('fixed', 'percentage') NOT NULL,
    discount_value      DECIMAL(15,2)   NOT NULL,
    promo_code          VARCHAR(50)     NULL UNIQUE,
    display_mode        ENUM('copy_code', 'auto_apply') NOT NULL DEFAULT 'copy_code',
    min_order_value     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    max_discount_amount DECIMAL(15,2)   NULL COMMENT 'Giới hạn số tiền giảm tối đa (dùng cho loại percentage)',
    usage_limit         INT UNSIGNED    NULL COMMENT 'NULL = không giới hạn lượt dùng',
    usage_count         INT UNSIGNED    NOT NULL DEFAULT 0,
    valid_from          DATE            NOT NULL,
    valid_to            DATE            NOT NULL,
    status              ENUM('active', 'expired', 'draft') NOT NULL DEFAULT 'draft',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 11. BOOKINGS — Đặt tour
-- =============================================================
CREATE TABLE bookings (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    booking_code    VARCHAR(20)     NOT NULL UNIQUE COMMENT 'Mã đặt chỗ hiển thị: BK-xxxx',
    user_id         INT UNSIGNED    NOT NULL,
    tour_id         INT UNSIGNED    NOT NULL,
    departure_id    INT UNSIGNED    NOT NULL,
    deal_id         INT UNSIGNED    NULL,
    guest_count     TINYINT UNSIGNED NOT NULL DEFAULT 1,
    subtotal        DECIMAL(15,2)   NOT NULL,
    tax_amount      DECIMAL(15,2)   NOT NULL DEFAULT 0,
    discount_amount DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_amount    DECIMAL(15,2)   NOT NULL,
    status          ENUM('pending', 'confirmed', 'cancelled', 'completed') NOT NULL DEFAULT 'pending',
    notes           TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_departure
        FOREIGN KEY (departure_id) REFERENCES tour_departures(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_deal
        FOREIGN KEY (deal_id) REFERENCES deals(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 12. ORDERS — Đơn hàng / giao dịch thanh toán
-- =============================================================
CREATE TABLE orders (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    order_code      VARCHAR(20)     NOT NULL UNIQUE,
    booking_id      INT UNSIGNED    NOT NULL,
    user_id         INT UNSIGNED    NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    payment_method  ENUM('credit_card', 'bank_transfer', 'momo', 'vnpay') NOT NULL,
    payment_status  ENUM('pending', 'paid', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    transaction_ref VARCHAR(255)    NULL COMMENT 'Mã tham chiếu từ cổng thanh toán',
    card_last_four  CHAR(4)         NULL COMMENT 'Chỉ lưu 4 số cuối thẻ',
    paid_at         TIMESTAMP       NULL,
    refunded_at     TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 13. REVIEWS — Đánh giá tour
-- =============================================================
CREATE TABLE reviews (
    id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    tour_id     INT UNSIGNED    NOT NULL,
    user_id     INT UNSIGNED    NOT NULL,
    booking_id  INT UNSIGNED    NOT NULL,
    rating      TINYINT UNSIGNED NOT NULL,
    comment     TEXT            NULL,
    is_verified BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_review_booking (booking_id),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_reviews_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 14. WISHLISTS — Tour yêu thích của người dùng
-- =============================================================
CREATE TABLE wishlists (
    id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    user_id     INT UNSIGNED    NOT NULL,
    tour_id     INT UNSIGNED    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_wishlist (user_id, tour_id),
    CONSTRAINT fk_wishlists_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_tour
        FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 15. BLOG_POSTS — Bài viết / nhật ký du lịch
-- =============================================================
CREATE TABLE blog_posts (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    author_id       INT UNSIGNED    NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    slug            VARCHAR(500)    NOT NULL UNIQUE,
    cover_image_url VARCHAR(500)    NULL,
    excerpt         TEXT            NULL,
    status          ENUM('draft', 'published', 'scheduled') NOT NULL DEFAULT 'draft',
    published_at    TIMESTAMP       NULL,
    scheduled_at    TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_blogs_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- 16. BLOG_BLOCKS — Các khối nội dung của bài viết (block editor)
-- =============================================================
CREATE TABLE blog_blocks (
    id              INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
    blog_post_id    INT UNSIGNED    NOT NULL,
    block_type      ENUM('paragraph', 'heading', 'quote', 'image', 'gallery') NOT NULL,
    content         TEXT            NULL COMMENT 'Text content hoặc HTML',
    image_url       VARCHAR(500)    NULL COMMENT 'Dùng cho block loại image',
    sort_order      SMALLINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blocks_post
        FOREIGN KEY (blog_post_id) REFERENCES blog_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- INDEXES — Tối ưu hiệu suất truy vấn
-- =============================================================

-- Tours
CREATE INDEX idx_tours_destination   ON tours(destination_id);
CREATE INDEX idx_tours_status        ON tours(status);
CREATE INDEX idx_tours_price         ON tours(price_per_person);

-- Tour Departures
CREATE INDEX idx_departures_tour     ON tour_departures(tour_id);
CREATE INDEX idx_departures_date     ON tour_departures(departure_date);
CREATE INDEX idx_departures_status   ON tour_departures(status);

-- Bookings
CREATE INDEX idx_bookings_user       ON bookings(user_id);
CREATE INDEX idx_bookings_tour       ON bookings(tour_id);
CREATE INDEX idx_bookings_status     ON bookings(status);
CREATE INDEX idx_bookings_code       ON bookings(booking_code);

-- Orders
CREATE INDEX idx_orders_booking      ON orders(booking_id);
CREATE INDEX idx_orders_user         ON orders(user_id);
CREATE INDEX idx_orders_status       ON orders(payment_status);

-- Deals
CREATE INDEX idx_deals_status        ON deals(status);
CREATE INDEX idx_deals_dates         ON deals(valid_from, valid_to);

-- Reviews
CREATE INDEX idx_reviews_tour        ON reviews(tour_id);
CREATE INDEX idx_reviews_user        ON reviews(user_id);

-- Blog
CREATE INDEX idx_blog_status         ON blog_posts(status);
CREATE INDEX idx_blog_author         ON blog_posts(author_id);

-- =============================================================
-- SEED DATA MẪU
-- =============================================================

-- Admin mặc định (password: Admin@123)
INSERT INTO users (email, password_hash, full_name, role)
VALUES ('admin@ptour.vn', '$2b$10$examplehashhere', 'Admin PTIT Tour', 'admin');

-- Điểm đến mẫu
INSERT INTO destinations (name, slug, region, is_featured) VALUES
('Đà Lạt',   'da-lat',   'Tây Nguyên',  TRUE),
('Hạ Long',  'ha-long',  'Đông Bắc Bộ', TRUE),
('Phú Quốc', 'phu-quoc', 'Nam Bộ',      TRUE),
('Hội An',   'hoi-an',   'Miền Trung',  TRUE),
('Sa Pa',    'sa-pa',    'Tây Bắc Bộ',  TRUE);
