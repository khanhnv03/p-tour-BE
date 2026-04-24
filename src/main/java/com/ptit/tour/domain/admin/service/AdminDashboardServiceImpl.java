package com.ptit.tour.domain.admin.service;

import com.ptit.tour.domain.admin.dto.*;
import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.contact.enums.ContactStatus;
import com.ptit.tour.domain.contact.repository.ContactMessageRepository;
import com.ptit.tour.domain.newsletter.repository.NewsletterSubscriptionRepository;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.repository.OrderRepository;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final OrderRepository orderRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Override
    public DashboardSummaryDto summary() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate previousMonthStart = monthStart.minusMonths(1);
        Instant currentFrom = startOfDay(monthStart);
        Instant currentTo = startOfDay(monthStart.plusMonths(1));
        Instant previousFrom = startOfDay(previousMonthStart);
        Instant previousTo = currentFrom;

        BigDecimal currentRevenue = orderRepository.sumPaidAmountBetween(currentFrom, currentTo);
        BigDecimal previousRevenue = orderRepository.sumPaidAmountBetween(previousFrom, previousTo);
        long currentCustomers = userRepository.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UserRole.CUSTOMER, currentFrom, currentTo);
        long previousCustomers = userRepository.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UserRole.CUSTOMER, previousFrom, previousTo);

        return new DashboardSummaryDto(
            orderRepository.sumTotalRevenue(),
            userRepository.countByRole(UserRole.CUSTOMER),
            tourRepository.countByStatus(TourStatus.PUBLISHED),
            tourRepository.averagePublishedRating(),
            growthPercent(currentRevenue, previousRevenue),
            growthPercent(BigDecimal.valueOf(currentCustomers), BigDecimal.valueOf(previousCustomers)),
            bookingRepository.countByStatus(BookingStatus.PENDING),
            contactMessageRepository.countByStatus(ContactStatus.NEW),
            newsletterSubscriptionRepository.countByActiveTrue()
        );
    }

    @Override
    public Page<BookingDto> recentBookings(Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findAll(sorted).map(BookingDto::from);
    }

    @Override
    public List<TopTourDto> topTours(int limit) {
        int size = Math.max(1, Math.min(limit, 20));
        return tourRepository.findPopular(PageRequest.of(0, size)).stream().map(TopTourDto::from).toList();
    }

    @Override
    public List<TimeSeriesPointDto> revenue(LocalDate from, LocalDate to) {
        DateRange range = range(from, to);
        return orderRepository.revenueByDay(range.from(), range.to()).stream()
            .map(row -> new TimeSeriesPointDto(toLocalDate(row[0]), (BigDecimal) row[1]))
            .toList();
    }

    @Override
    public BookingAnalyticsDto bookings(LocalDate from, LocalDate to) {
        DateRange range = range(from, to);
        List<TimeSeriesPointDto> daily = bookingRepository.bookingsByDay(range.from(), range.to()).stream()
            .map(row -> new TimeSeriesPointDto(toLocalDate(row[0]), BigDecimal.valueOf((Long) row[1])))
            .toList();
        return new BookingAnalyticsDto(
            bookingRepository.count(),
            bookingRepository.countByStatus(BookingStatus.CONFIRMED),
            bookingRepository.countByStatus(BookingStatus.CANCELLED),
            daily
        );
    }

    @Override
    public ConversionAnalyticsDto conversion() {
        long totalBookings = bookingRepository.count();
        long paidOrders = orderRepository.countByPaymentStatus(PaymentStatus.PAID);
        long pendingOrders = orderRepository.countByPaymentStatus(PaymentStatus.PENDING);
        long refundedOrders = orderRepository.countByPaymentStatus(PaymentStatus.REFUNDED);
        double rate = totalBookings == 0 ? 0 : BigDecimal.valueOf(paidOrders)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
            .doubleValue();
        return new ConversionAnalyticsDto(totalBookings, paidOrders, pendingOrders, refundedOrders, rate);
    }

    private DateRange range(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(ZONE);
        LocalDate start = from != null ? from : end.minusDays(30);
        return new DateRange(startOfDay(start), startOfDay(end.plusDays(1)));
    }

    private Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }

    private double growthPercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
            .multiply(BigDecimal.valueOf(100))
            .divide(previous, 2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private record DateRange(Instant from, Instant to) {}
}
