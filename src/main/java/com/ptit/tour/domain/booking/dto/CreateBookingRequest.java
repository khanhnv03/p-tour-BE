package com.ptit.tour.domain.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
    @NotNull Long tourId,
    @NotNull Long departureId,
    Long dealId,
    @Min(1) int guestCount,
    @Size(max = 255) String contactName,
    @Email @Size(max = 255) String contactEmail,
    @Size(max = 30) String contactPhone,
    String promoCode,
    String notes
) {}
