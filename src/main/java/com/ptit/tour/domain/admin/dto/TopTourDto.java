package com.ptit.tour.domain.admin.dto;

import com.ptit.tour.domain.tour.entity.Tour;

import java.math.BigDecimal;

public record TopTourDto(
    Long id,
    String title,
    String slug,
    String coverImageUrl,
    String destinationName,
    int bookingCount,
    BigDecimal rating,
    int reviewCount
) {
    public static TopTourDto from(Tour tour) {
        return new TopTourDto(
            tour.getId(), tour.getTitle(), tour.getSlug(), tour.getCoverImageUrl(),
            tour.getDestination().getName(), tour.getBookingCount(), tour.getRating(), tour.getReviewCount()
        );
    }
}
