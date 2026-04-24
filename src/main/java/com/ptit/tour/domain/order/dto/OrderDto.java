package com.ptit.tour.domain.order.dto;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record OrderDto(
    Long id,
    String orderCode,
    Long bookingId,
    String bookingCode,
    Long userId,
    String userName,
    String tourTitle,
    String tourCoverImage,
    LocalDate departureDate,
    int guestCount,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    String transactionRef,
    String cardLastFour,
    Instant paidAt,
    Instant refundedAt,
    Instant createdAt
) {
    public static OrderDto from(Order o) {
        Booking b = o.getBooking();
        return new OrderDto(
            o.getId(), o.getOrderCode(),
            b.getId(), b.getBookingCode(),
            o.getUser().getId(), o.getUser().getFullName(),
            b.getTour().getTitle(), b.getTour().getCoverImageUrl(),
            b.getDeparture().getDepartureDate(), b.getGuestCount(),
            o.getAmount(), o.getPaymentMethod(), o.getPaymentStatus(),
            o.getTransactionRef(), o.getCardLastFour(),
            o.getPaidAt(), o.getRefundedAt(), o.getCreatedAt()
        );
    }
}
