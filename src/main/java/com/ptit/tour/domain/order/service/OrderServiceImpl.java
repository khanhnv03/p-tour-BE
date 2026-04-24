package com.ptit.tour.domain.order.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.common.mail.EmailService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderDto create(Long userId, CreateOrderRequest req) {
        if (orderRepository.findByBookingId(req.bookingId()).isPresent()) {
            throw new BusinessException("Booking này đã có đơn hàng thanh toán");
        }
        Booking booking = bookingService.getEntityById(req.bookingId());
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException("Không có quyền tạo đơn hàng cho booking này", HttpStatus.FORBIDDEN);
        }
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
    @Transactional(readOnly = true)
    public OrderDto getById(Long id, Long requestingUserId) {
        Order order = getEntityById(id);
        if (!order.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền xem đơn hàng này", HttpStatus.FORBIDDEN);
        }
        return OrderDto.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getByBookingId(Long bookingId, Long requestingUserId) {
        Order order = orderRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "bookingId", bookingId));
        if (!order.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền xem đơn hàng này", HttpStatus.FORBIDDEN);
        }
        return OrderDto.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getByIdAdmin(Long id) {
        return OrderDto.from(getEntityById(id));
    }

    @Override
    @Transactional
    public OrderDto confirmPayment(Long id, String transactionRef, String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        if (normalizedKey != null) {
            var existing = orderRepository.findByPaymentIdempotencyKey(normalizedKey);
            if (existing.isPresent()) {
                Order order = existing.get();
                if (!order.getId().equals(id)) {
                    throw new BusinessException("Idempotency-Key đã được dùng cho đơn hàng khác", HttpStatus.CONFLICT);
                }
                return OrderDto.from(order);
            }
        }

        Order order = getEntityById(id);
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            if (normalizedKey != null && normalizedKey.equals(order.getPaymentIdempotencyKey())) {
                return OrderDto.from(order);
            }
            throw new BusinessException("Đơn hàng không ở trạng thái chờ thanh toán");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setTransactionRef(transactionRef);
        order.setPaymentIdempotencyKey(normalizedKey);
        order.setPaidAt(Instant.now());
        // Confirm linked booking — also increments deal.usageCount inside updateStatus
        bookingService.updateStatus(order.getBooking().getId(), BookingStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);
        sendBookingConfirmationEmail(savedOrder);
        return OrderDto.from(savedOrder);
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
        // Cancel linked booking — also decrements deal.usageCount inside updateStatus
        bookingService.updateStatus(order.getBooking().getId(), BookingStatus.CANCELLED);
        return OrderDto.from(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(Long userId, PaymentStatus status, Pageable pageable) {
        if (userId != null && status != null) {
            return orderRepository.findByUserIdAndPaymentStatus(userId, status, pageable).map(OrderDto::from);
        }
        if (userId != null) {
            return orderRepository.findByUserId(userId, pageable).map(OrderDto::from);
        }
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

    private void sendBookingConfirmationEmail(Order order) {
        Booking booking = order.getBooking();
        emailService.sendBookingConfirmationEmail(
            order.getUser().getEmail(),
            order.getUser().getFullName(),
            order.getOrderCode(),
            booking.getBookingCode(),
            booking.getTour().getTitle(),
            booking.getDeparture().getDepartureDate(),
            booking.getGuestCount(),
            order.getAmount()
        );
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        String key = idempotencyKey.trim();
        if (key.length() > 120) {
            throw new BusinessException("Idempotency-Key quá dài", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return key;
    }
}
