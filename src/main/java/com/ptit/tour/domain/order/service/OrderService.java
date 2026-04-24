package com.ptit.tour.domain.order.service;

import com.ptit.tour.domain.order.dto.CreateOrderRequest;
import com.ptit.tour.domain.order.dto.OrderDto;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto create(Long userId, CreateOrderRequest request);
    /** Xem order của mình — ném FORBIDDEN nếu không phải owner. */
    OrderDto getById(Long id, Long requestingUserId);
    /** Xem order theo bookingId — kiểm tra booking thuộc về user. */
    OrderDto getByBookingId(Long bookingId, Long requestingUserId);
    /** Xác nhận thanh toán từ payment gateway (webhook). */
    OrderDto confirmPayment(Long id, String transactionRef, String idempotencyKey);
    OrderDto refund(Long id);
    // Admin
    OrderDto getByIdAdmin(Long id);
    Page<OrderDto> findAll(Long userId, PaymentStatus status, Pageable pageable);
    Order getEntityById(Long id);
}
