package com.ptit.tour.domain.order.repository;

import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByBookingId(Long bookingId);

    Optional<Order> findByPaymentIdempotencyKey(String paymentIdempotencyKey);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    Page<Order> findByUserIdAndPaymentStatus(Long userId, PaymentStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.user.id = :userId")
    BigDecimal sumPaidAmountByUserId(@Param("userId") Long userId);

    long countByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.paymentStatus = 'PAID' AND o.paidAt >= :from AND o.paidAt < :to")
    BigDecimal sumPaidAmountBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT FUNCTION('DATE', o.paidAt), COALESCE(SUM(o.amount), 0)
        FROM Order o
        WHERE o.paymentStatus = 'PAID'
          AND o.paidAt >= :from
          AND o.paidAt < :to
        GROUP BY FUNCTION('DATE', o.paidAt)
        ORDER BY FUNCTION('DATE', o.paidAt)
        """)
    List<Object[]> revenueByDay(@Param("from") Instant from, @Param("to") Instant to);
}
