package com.ptit.tour.domain.booking.service;

import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.dto.TicketPdfDownload;
import com.ptit.tour.domain.booking.dto.TicketDto;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto create(Long userId, CreateBookingRequest request);
    /** Xem booking của mình — ném FORBIDDEN nếu không phải owner. */
    BookingDto getById(Long id, Long requestingUserId);
    /** Xem bất kỳ booking nào (admin). */
    BookingDto getByIdAdmin(Long id);
    Page<BookingDto> getMyBookings(Long userId, Pageable pageable);
    BookingDto cancel(Long id, Long requestingUserId);
    /** E-ticket của booking đã confirmed. */
    TicketDto getTicket(Long id, Long requestingUserId);
    TicketPdfDownload getTicketPdf(Long id, Long requestingUserId, boolean adminAccess, String token);
    // Admin
    Page<BookingDto> findAll(Long userId, BookingStatus status, Pageable pageable);
    BookingDto updateStatus(Long id, BookingStatus status);
    Booking getEntityById(Long id);
}
