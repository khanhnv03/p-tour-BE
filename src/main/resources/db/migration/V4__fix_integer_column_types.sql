ALTER TABLE tours
    MODIFY COLUMN duration_days INT NOT NULL,
    MODIFY COLUMN duration_nights INT NOT NULL,
    MODIFY COLUMN max_guests INT NOT NULL;

ALTER TABLE itinerary_days
    MODIFY COLUMN day_number INT NOT NULL;

ALTER TABLE tour_departures
    MODIFY COLUMN available_slots INT NOT NULL,
    MODIFY COLUMN booked_slots INT NOT NULL DEFAULT 0;

ALTER TABLE bookings
    MODIFY COLUMN guest_count INT NOT NULL;

ALTER TABLE reviews
    MODIFY COLUMN rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5);
