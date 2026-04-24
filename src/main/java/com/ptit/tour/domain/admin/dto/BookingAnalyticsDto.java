package com.ptit.tour.domain.admin.dto;

import java.util.List;

public record BookingAnalyticsDto(
    long totalBookings,
    long confirmedBookings,
    long cancelledBookings,
    List<TimeSeriesPointDto> dailyBookings
) {}
