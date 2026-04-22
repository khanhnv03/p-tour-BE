package com.ptit.tour.domain.tour.dto;

import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;

import java.math.BigDecimal;

public record TourSummaryDto(
    Long id,
    String title,
    String slug,
    String coverImageUrl,
    String destinationName,
    int durationDays,
    int durationNights,
    TourDifficulty difficulty,
    BigDecimal pricePerPerson,
    TourStatus status,
    BigDecimal rating,
    int reviewCount,
    int bookingCount
) {
    public static TourSummaryDto from(Tour t) {
        return new TourSummaryDto(
            t.getId(), t.getTitle(), t.getSlug(), t.getCoverImageUrl(),
            t.getDestination().getName(),
            t.getDurationDays(), t.getDurationNights(),
            t.getDifficulty(), t.getPricePerPerson(),
            t.getStatus(), t.getRating(), t.getReviewCount(), t.getBookingCount()
        );
    }
}
