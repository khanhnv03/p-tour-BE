package com.ptit.tour.domain.booking.service;

import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto create(Long userId, CreateBookingRequest request);
    BookingDto getById(Long id);
    Page<BookingDto> getMyBookings(Long userId, Pageable pageable);
    BookingDto cancel(Long id, Long requestingUserId);
    // Admin
    Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable);
    BookingDto updateStatus(Long id, BookingStatus status);
    Booking getEntityById(Long id);
}
