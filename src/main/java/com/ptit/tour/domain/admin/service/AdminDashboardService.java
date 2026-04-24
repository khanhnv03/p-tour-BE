package com.ptit.tour.domain.admin.service;

import com.ptit.tour.domain.admin.dto.*;
import com.ptit.tour.domain.booking.dto.BookingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AdminDashboardService {
    DashboardSummaryDto summary();
    Page<BookingDto> recentBookings(Pageable pageable);
    List<TopTourDto> topTours(int limit);
    List<TimeSeriesPointDto> revenue(LocalDate from, LocalDate to);
    BookingAnalyticsDto bookings(LocalDate from, LocalDate to);
    ConversionAnalyticsDto conversion();
}
