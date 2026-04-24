ALTER TABLE bookings
    ADD COLUMN contact_name VARCHAR(255) NULL AFTER guest_count,
    ADD COLUMN contact_email VARCHAR(255) NULL AFTER contact_name,
    ADD COLUMN contact_phone VARCHAR(30) NULL AFTER contact_email;

UPDATE bookings b
JOIN users u ON u.id = b.user_id
SET b.contact_name = COALESCE(NULLIF(TRIM(b.contact_name), ''), u.full_name),
    b.contact_email = COALESCE(NULLIF(TRIM(b.contact_email), ''), u.email),
    b.contact_phone = COALESCE(NULLIF(TRIM(b.contact_phone), ''), u.phone);

ALTER TABLE bookings
    MODIFY COLUMN contact_name VARCHAR(255) NOT NULL,
    MODIFY COLUMN contact_email VARCHAR(255) NOT NULL;
