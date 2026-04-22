package com.ptit.tour.domain.review.dto;

import com.ptit.tour.domain.review.entity.Review;

import java.time.Instant;

public record ReviewDto(
    Long id,
    Long tourId,
    Long userId,
    String userFullName,
    String userAvatarUrl,
    Long bookingId,
    int rating,
    String comment,
    boolean verified,
    Instant createdAt
) {
    public static ReviewDto from(Review r) {
        return new ReviewDto(
            r.getId(), r.getTour().getId(),
            r.getUser().getId(), r.getUser().getFullName(), r.getUser().getAvatarUrl(),
            r.getBooking().getId(), r.getRating(), r.getComment(),
            r.isVerified(), r.getCreatedAt()
        );
    }
}
