package com.ptit.tour.domain.order.service;

import com.ptit.tour.domain.order.dto.CreateOrderRequest;
import com.ptit.tour.domain.order.dto.OrderDto;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto create(Long userId, CreateOrderRequest request);
    OrderDto getById(Long id);
    OrderDto getByBookingId(Long bookingId);
    /** Simulate payment confirmation (replace with real gateway callback). */
    OrderDto confirmPayment(Long id, String transactionRef);
    OrderDto refund(Long id);
    // Admin
    Page<OrderDto> findAll(PaymentStatus status, Pageable pageable);
    Order getEntityById(Long id);
}
