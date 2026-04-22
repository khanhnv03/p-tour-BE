package com.ptit.tour.domain.booking.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.repository.DealRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.util.BookingCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.05");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourDepartureRepository departureRepository;
    private final DealRepository dealRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, CreateBookingRequest req) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Tour tour = tourRepository.findById(req.tourId())
            .orElseThrow(() -> new ResourceNotFoundException("Tour", req.tourId()));
        TourDeparture departure = departureRepository.findById(req.departureId())
            .orElseThrow(() -> new ResourceNotFoundException("Departure", req.departureId()));

        if (departure.getStatus() != DepartureStatus.OPEN || !departure.hasAvailableSlots()) {
            throw new BusinessException("Lịch khởi hành đã hết chỗ hoặc không còn nhận đặt");
        }
        if (req.guestCount() > departure.getAvailableSlots() - departure.getBookedSlots()) {
            throw new BusinessException("Số lượng khách vượt quá chỗ còn lại");
        }

        BigDecimal unitPrice = departure.effectivePrice(tour.getPricePerPerson());
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.guestCount()));
        BigDecimal taxAmount = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        Deal deal = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (req.dealId() != null) {
            deal = dealRepository.findById(req.dealId()).orElse(null);
            if (deal != null) discountAmount = deal.computeDiscount(subtotal);
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);

        String bookingCode;
        do { bookingCode = BookingCodeGenerator.generate(); }
        while (bookingRepository.findByBookingCode(bookingCode).isPresent());

        Booking booking = Booking.builder()
            .bookingCode(bookingCode).user(user).tour(tour).departure(departure).deal(deal)
            .guestCount(req.guestCount()).subtotal(subtotal).taxAmount(taxAmount)
            .discountAmount(discountAmount).totalAmount(totalAmount).notes(req.notes())
            .build();

        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long id) {
        return BookingDto.from(getEntityById(id));
    }

    @Override
    public Page<BookingDto> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(BookingDto::from);
    }

    @Override
    @Transactional
    public BookingDto cancel(Long id, Long requestingUserId) {
        Booking booking = getEntityById(id);
        if (!booking.getUser().getId().equals(requestingUserId)) {
            throw new BusinessException("Không có quyền huỷ booking này", HttpStatus.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Chỉ có thể huỷ booking ở trạng thái chờ xác nhận");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    public Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable) {
        return bookingRepository.findAllFiltered(userId, status, pageable).map(BookingDto::from);
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = getEntityById(id);
        booking.setStatus(status);
        if (status == BookingStatus.CONFIRMED) {
            TourDeparture dep = booking.getDeparture();
            dep.setBookedSlots(dep.getBookedSlots() + booking.getGuestCount());
            if (dep.getBookedSlots() >= dep.getAvailableSlots()) {
                dep.setStatus(DepartureStatus.FULL);
            }
        }
        if (status == BookingStatus.COMPLETED) {
            Tour t = booking.getTour();
            t.setBookingCount(t.getBookingCount() + 1);
        }
        if (status == BookingStatus.CANCELLED) {
            TourDeparture dep = booking.getDeparture();
            int restored = Math.max(0, dep.getBookedSlots() - booking.getGuestCount());
            dep.setBookedSlots(restored);
            if (dep.getStatus() == DepartureStatus.FULL) {
                dep.setStatus(DepartureStatus.OPEN);
            }
        }
        return BookingDto.from(bookingRepository.save(booking));
    }

    @Override
    public Booking getEntityById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }
}
