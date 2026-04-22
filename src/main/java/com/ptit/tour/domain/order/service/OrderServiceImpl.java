package com.ptit.tour.domain.order.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.service.BookingService;
import com.ptit.tour.domain.order.dto.CreateOrderRequest;
import com.ptit.tour.domain.order.dto.OrderDto;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.util.BookingCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderDto create(Long userId, CreateOrderRequest req) {
        if (orderRepository.findByBookingId(req.bookingId()).isPresent()) {
            throw new BusinessException("Booking này đã có đơn hàng thanh toán");
        }
        Booking booking = bookingService.getEntityById(req.bookingId());
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Booking không ở trạng thái chờ thanh toán");
        }
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String orderCode = BookingCodeGenerator.generateOrderCode();
        Order order = Order.builder()
            .orderCode(orderCode).booking(booking).user(user)
            .amount(booking.getTotalAmount())
            .paymentMethod(req.paymentMethod())
            .cardLastFour(req.cardLastFour())
            .build();
        return OrderDto.from(orderRepository.save(order));
    }

    @Override
    public OrderDto getById(Long id) {
        return OrderDto.from(getEntityById(id));
    }

    @Override
    public OrderDto getByBookingId(Long bookingId) {
        return orderRepository.findByBookingId(bookingId)
            .map(OrderDto::from)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "bookingId", bookingId));
    }

    @Override
    @Transactional
    public OrderDto confirmPayment(Long id, String transactionRef) {
        Order order = getEntityById(id);
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Đơn hàng không ở trạng thái chờ thanh toán");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setTransactionRef(transactionRef);
        order.setPaidAt(Instant.now());
        // Confirm the linked booking
        bookingService.updateStatus(order.getBooking().getId(), BookingStatus.CONFIRMED);
        return OrderDto.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto refund(Long id) {
        Order order = getEntityById(id);
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("Chỉ có thể hoàn tiền đơn hàng đã thanh toán");
        }
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setRefundedAt(Instant.now());
        bookingService.updateStatus(order.getBooking().getId(), BookingStatus.CANCELLED);
        return OrderDto.from(orderRepository.save(order));
    }

    @Override
    public Page<OrderDto> findAll(PaymentStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByPaymentStatus(status, pageable).map(OrderDto::from);
        }
        return orderRepository.findAll(pageable).map(OrderDto::from);
    }

    @Override
    public Order getEntityById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
