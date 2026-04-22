package com.ptit.tour.domain.booking.dto;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingDto(
    Long id,
    String bookingCode,
    Long userId,
    String userName,
    Long tourId,
    String tourTitle,
    String tourCoverImage,
    Long departureId,
    LocalDate departureDate,
    Long dealId,
    int guestCount,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    BookingStatus status,
    String notes,
    Instant createdAt
) {
    public static BookingDto from(Booking b) {
        return new BookingDto(
            b.getId(), b.getBookingCode(),
            b.getUser().getId(), b.getUser().getFullName(),
            b.getTour().getId(), b.getTour().getTitle(), b.getTour().getCoverImageUrl(),
            b.getDeparture().getId(), b.getDeparture().getDepartureDate(),
            b.getDeal() != null ? b.getDeal().getId() : null,
            b.getGuestCount(), b.getSubtotal(), b.getTaxAmount(),
            b.getDiscountAmount(), b.getTotalAmount(), b.getStatus(),
            b.getNotes(), b.getCreatedAt()
        );
    }
}
