package com.ptit.tour.config;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.destination.repository.DestinationRepository;
import com.ptit.tour.domain.order.entity.Order;
import com.ptit.tour.domain.order.enums.PaymentMethod;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private static final String CUSTOMER_EMAIL = "alex@ptittour.com";
    private static final String CUSTOMER_PASSWORD = "Customer@123456";
    private static final String DESTINATION_SLUG = "da-lat";
    private static final String TOUR_SLUG = "binh-minh-tren-dinh-langbiang";
    private static final String BOOKING_CODE = "BK-1934";
    private static final String ORDER_CODE = "ORD-1934";

    private final UserRepository userRepository;
    private final DestinationRepository destinationRepository;
    private final TourRepository tourRepository;
    private final TourDepartureRepository departureRepository;
    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL)
            .orElseGet(this::createCustomer);

        Destination destination = destinationRepository.findBySlug(DESTINATION_SLUG)
            .orElseGet(this::createDestination);

        Tour tour = tourRepository.findBySlug(TOUR_SLUG)
            .orElseGet(() -> createTour(destination));

        TourDeparture departure = departureRepository.findByTourIdOrderByDepartureDateAsc(tour.getId()).stream()
            .filter(item -> item.getDepartureDate().equals(LocalDate.of(2026, 5, 20)))
            .findFirst()
            .orElseGet(() -> createDeparture(tour));

        Booking booking = bookingRepository.findByBookingCode(BOOKING_CODE)
            .orElseGet(() -> createBooking(customer, tour, departure));

        orderRepository.findByBookingId(booking.getId())
            .orElseGet(() -> createOrder(customer, booking));

        log.info("Dev Phase 3 seed ready: customer={}, bookingCode={}", CUSTOMER_EMAIL, BOOKING_CODE);
    }

    private User createCustomer() {
        User customer = User.builder()
            .email(CUSTOMER_EMAIL)
            .passwordHash(passwordEncoder.encode(CUSTOMER_PASSWORD))
            .fullName("Alex PTIT")
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
        return userRepository.save(customer);
    }

    private Destination createDestination() {
        Destination destination = Destination.builder()
            .name("Đà Lạt")
            .slug(DESTINATION_SLUG)
            .description("Điểm đến seed cho flow checkout/order Phase 3.")
            .coverImageUrl("https://images.unsplash.com/photo-1528127269322-539801943592")
            .country("Việt Nam")
            .region("Tây Nguyên")
            .featured(true)
            .tourCount(1)
            .build();
        return destinationRepository.save(destination);
    }

    private Tour createTour(Destination destination) {
        Tour tour = Tour.builder()
            .destination(destination)
            .title("Bình minh trên đỉnh Langbiang")
            .slug(TOUR_SLUG)
            .description("Tour seed để kiểm tra end-to-end checkout, booking và order.")
            .durationDays(3)
            .durationNights(2)
            .maxGuests(10)
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .coverImageUrl("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee")
            .status(TourStatus.PUBLISHED)
            .rating(BigDecimal.valueOf(4.8))
            .reviewCount(12)
            .bookingCount(1)
            .build();
        return tourRepository.save(tour);
    }

    private TourDeparture createDeparture(Tour tour) {
        TourDeparture departure = TourDeparture.builder()
            .tour(tour)
            .departureDate(LocalDate.of(2026, 5, 20))
            .availableSlots(12)
            .bookedSlots(2)
            .status(DepartureStatus.OPEN)
            .build();
        return departureRepository.save(departure);
    }

    private Booking createBooking(User customer, Tour tour, TourDeparture departure) {
        Booking booking = Booking.builder()
            .bookingCode(BOOKING_CODE)
            .user(customer)
            .tour(tour)
            .departure(departure)
            .guestCount(2)
            .contactName("Alex PTIT")
            .contactEmail(CUSTOMER_EMAIL)
            .contactPhone("+84 912 345 678")
            .subtotal(BigDecimal.valueOf(6_980_000L))
            .taxAmount(BigDecimal.valueOf(349_000L))
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.valueOf(7_329_000L))
            .status(BookingStatus.CONFIRMED)
            .notes("Seed booking for Phase 3 verification")
            .build();
        return bookingRepository.save(booking);
    }

    private Order createOrder(User customer, Booking booking) {
        Order order = Order.builder()
            .orderCode(ORDER_CODE)
            .booking(booking)
            .user(customer)
            .amount(booking.getTotalAmount())
            .paymentMethod(PaymentMethod.VNPAY)
            .paymentStatus(PaymentStatus.PAID)
            .transactionRef("SEED-TXN-BK1934")
            .paidAt(Instant.parse("2026-04-24T00:00:00Z"))
            .build();
        return orderRepository.save(order);
    }
}
