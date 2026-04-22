package com.ptit.tour.domain.order.repository;

import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByBookingId(Long bookingId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    BigDecimal sumTotalRevenue();
}
