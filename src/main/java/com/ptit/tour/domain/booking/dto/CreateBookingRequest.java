package com.ptit.tour.domain.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
    @NotNull Long tourId,
    @NotNull Long departureId,
    Long dealId,
    @Min(1) int guestCount,
    String promoCode,
    String notes
) {}
