package com.ptit.tour.domain.order.entity;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.shared.BaseEntity;
import com.ptit.tour.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_status", columnList = "payment_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "order_code", unique = true, nullable = false, length = 20)
    private String orderCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_ref", length = 255)
    private String transactionRef;

    @Column(name = "payment_idempotency_key", length = 120, unique = true)
    private String paymentIdempotencyKey;

    /** Last 4 digits only — never store full card number (PCI DSS). */
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;
}
