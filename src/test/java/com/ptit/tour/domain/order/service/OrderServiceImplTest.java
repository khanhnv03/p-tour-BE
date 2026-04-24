package com.ptit.tour.domain.order.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.mail.EmailService;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.service.BookingService;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void confirmPaymentShouldMarkOrderPaidAndConfirmBooking() {
        Order order = buildOrder(15L, 7L, PaymentStatus.PENDING);

        when(orderRepository.findById(15L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = orderService.confirmPayment(15L, "TXN-2026-0001", "pay-15");

        assertThat(dto.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(dto.transactionRef()).isEqualTo("TXN-2026-0001");
        assertThat(dto.paidAt()).isNotNull();
        verify(bookingService).updateStatus(31L, BookingStatus.CONFIRMED);
        verify(emailService).sendBookingConfirmationEmail(
            eq("alex@ptittour.com"),
            eq("Alex PTIT"),
            eq("ORD-20260001"),
            eq("BK-1934"),
            eq("Bình minh trên đỉnh Langbiang"),
            eq(LocalDate.of(2026, 5, 20)),
            eq(2),
            eq(BigDecimal.valueOf(7_329_000L))
        );
    }

    @Test
    void confirmPaymentShouldReturnExistingOrderForSameIdempotencyKey() {
        Order order = buildOrder(15L, 7L, PaymentStatus.PAID);
        order.setPaymentIdempotencyKey("pay-15");

        when(orderRepository.findByPaymentIdempotencyKey("pay-15")).thenReturn(Optional.of(order));

        var dto = orderService.confirmPayment(15L, "TXN-IGNORED", "pay-15");

        assertThat(dto.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        verifyNoInteractions(bookingService);
    }

    @Test
    void getByIdShouldRejectOrderFromAnotherUser() {
        Order order = buildOrder(15L, 7L, PaymentStatus.PENDING);

        when(orderRepository.findById(15L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.getById(15L, 99L));
    }

    private Order buildOrder(Long orderId, Long userId, PaymentStatus paymentStatus) {
        User user = User.builder()
            .email("alex@ptittour.com")
            .fullName("Alex PTIT")
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
        user.setId(userId);

        Tour tour = Tour.builder()
            .title("Bình minh trên đỉnh Langbiang")
            .slug("binh-minh-tren-dinh-langbiang")
            .durationDays(3)
            .durationNights(2)
            .maxGuests(10)
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .coverImageUrl("https://example.com/langbiang.jpg")
            .status(TourStatus.PUBLISHED)
            .build();
        tour.setId(3L);

        TourDeparture departure = TourDeparture.builder()
            .tour(tour)
            .departureDate(LocalDate.of(2026, 5, 20))
            .availableSlots(12)
            .bookedSlots(0)
            .status(DepartureStatus.OPEN)
            .build();
        departure.setId(11L);

        Booking booking = Booking.builder()
            .bookingCode("BK-1934")
            .user(user)
            .tour(tour)
            .departure(departure)
            .guestCount(2)
            .totalAmount(BigDecimal.valueOf(7_329_000L))
            .status(BookingStatus.PENDING)
            .build();
        booking.setId(31L);

        Order order = Order.builder()
            .orderCode("ORD-20260001")
            .booking(booking)
            .user(user)
            .amount(booking.getTotalAmount())
            .paymentMethod(PaymentMethod.VNPAY)
            .paymentStatus(paymentStatus)
            .build();
        order.setId(orderId);
        return order;
    }
}
