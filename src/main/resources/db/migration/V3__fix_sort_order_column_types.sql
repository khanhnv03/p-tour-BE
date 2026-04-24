ALTER TABLE tour_gallery_images
    MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE tour_highlights
    MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE tour_inclusions
    MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE itinerary_activities
    MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0;

ALTER TABLE blog_blocks
    MODIFY COLUMN sort_order INT NOT NULL DEFAULT 0;
