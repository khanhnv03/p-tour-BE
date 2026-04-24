package com.ptit.tour.domain.booking.dto;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TicketDto(
    String bookingCode,
    String tourTitle,
    String tourCoverImage,
    String destinationName,
    LocalDate departureDate,
    int durationDays,
    int durationNights,
    int guestCount,
    BigDecimal totalAmount,
    BookingStatus status,
    String customerName,
    String customerEmail,
    String customerPhone,
    String downloadUrl,
    String qrCodeData,
    Instant issuedAt
) {
    public static TicketDto from(Booking b, String downloadUrl, String qrCodeData) {
        return new TicketDto(
            b.getBookingCode(),
            b.getTour().getTitle(),
            b.getTour().getCoverImageUrl(),
            b.getTour().getDestination().getName(),
            b.getDeparture().getDepartureDate(),
            b.getTour().getDurationDays(),
            b.getTour().getDurationNights(),
            b.getGuestCount(),
            b.getTotalAmount(),
            b.getStatus(),
            b.getContactName(),
            b.getContactEmail(),
            b.getContactPhone(),
            downloadUrl,
            qrCodeData,
            Instant.now()
        );
    }
}
