-- Phase 2: review moderation, itinerary activity title, departure index

ALTER TABLE reviews
    ADD COLUMN review_status ENUM('PENDING','APPROVED','HIDDEN') NOT NULL DEFAULT 'PENDING';

ALTER TABLE itinerary_activities
    ADD COLUMN title VARCHAR(255) NULL;

CREATE INDEX idx_tour_departures_date_status ON tour_departures(departure_date, status);
