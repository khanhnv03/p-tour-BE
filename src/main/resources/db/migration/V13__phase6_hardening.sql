ALTER TABLE tour_departures
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN payment_idempotency_key VARCHAR(120) NULL,
    ADD UNIQUE KEY uk_orders_payment_idempotency_key (payment_idempotency_key);
