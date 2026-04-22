package com.ptit.tour.domain.order.dto;

import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderDto(
    Long id,
    String orderCode,
    Long bookingId,
    String bookingCode,
    Long userId,
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
        return new OrderDto(
            o.getId(), o.getOrderCode(),
            o.getBooking().getId(), o.getBooking().getBookingCode(),
            o.getUser().getId(), o.getAmount(),
            o.getPaymentMethod(), o.getPaymentStatus(),
            o.getTransactionRef(), o.getCardLastFour(),
            o.getPaidAt(), o.getRefundedAt(), o.getCreatedAt()
        );
    }
}
