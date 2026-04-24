package com.ptit.tour.domain.user.dto;

import com.ptit.tour.domain.booking.dto.BookingDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminCustomerDetailDto(
    UserDto user,
    long bookingCount,
    BigDecimal totalSpent,
    Instant lastBookingAt,
    List<BookingDto> recentBookings
) {}
