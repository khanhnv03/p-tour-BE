package com.ptit.tour.domain.admin.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.admin.dto.*;
import com.ptit.tour.domain.admin.service.AdminDashboardService;
import com.ptit.tour.domain.booking.dto.BookingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin Dashboard / Analytics")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "[Admin] Tổng quan dashboard")
    @GetMapping("/dashboard/summary")
    public ApiResponse<DashboardSummaryDto> summary() {
        return ApiResponse.ok(adminDashboardService.summary());
    }

    @Operation(summary = "[Admin] Booking gần đây")
    @GetMapping("/dashboard/recent-bookings")
    public ApiResponse<PageResponse<BookingDto>> recentBookings(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(adminDashboardService.recentBookings(pageable)));
    }

    @Operation(summary = "[Admin] Tour bán chạy")
    @GetMapping("/dashboard/top-tours")
    public ApiResponse<List<TopTourDto>> topTours(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(adminDashboardService.topTours(limit));
    }

    @Operation(summary = "[Admin] Analytics doanh thu theo ngày")
    @GetMapping("/analytics/revenue")
    public ApiResponse<List<TimeSeriesPointDto>> revenue(@RequestParam(required = false) LocalDate from,
                                                         @RequestParam(required = false) LocalDate to) {
        return ApiResponse.ok(adminDashboardService.revenue(from, to));
    }

    @Operation(summary = "[Admin] Analytics booking")
    @GetMapping("/analytics/bookings")
    public ApiResponse<BookingAnalyticsDto> bookings(@RequestParam(required = false) LocalDate from,
                                                     @RequestParam(required = false) LocalDate to) {
        return ApiResponse.ok(adminDashboardService.bookings(from, to));
    }

    @Operation(summary = "[Admin] Analytics conversion")
    @GetMapping("/analytics/conversion")
    public ApiResponse<ConversionAnalyticsDto> conversion() {
        return ApiResponse.ok(adminDashboardService.conversion());
    }
}
