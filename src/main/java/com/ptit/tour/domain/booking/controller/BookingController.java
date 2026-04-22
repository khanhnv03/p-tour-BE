package com.ptit.tour.domain.booking.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.booking.dto.BookingDto;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookings")
@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Tạo booking mới")
    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody CreateBookingRequest request) {
        return ApiResponse.created(bookingService.create(principal.getId(), request));
    }

    @Operation(summary = "Xem chi tiết booking")
    @GetMapping("/bookings/{id}")
    public ApiResponse<BookingDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getById(id));
    }

    @Operation(summary = "Lịch sử đặt tour của tôi")
    @GetMapping("/bookings/my")
    public ApiResponse<PageResponse<BookingDto>> myBookings(
        @AuthenticationPrincipal UserPrincipal principal,
        @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(bookingService.getMyBookings(principal.getId(), pageable)));
    }

    @Operation(summary = "Huỷ booking")
    @PatchMapping("/bookings/{id}/cancel")
    public ApiResponse<BookingDto> cancel(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(bookingService.cancel(id, principal.getId()));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Danh sách bookings")
    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<BookingDto>> listAll(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) BookingStatus status,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(bookingService.findAll(userId, status, pageable)));
    }

    @Operation(summary = "[Admin] Cập nhật trạng thái booking")
    @PatchMapping("/admin/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookingDto> updateStatus(@PathVariable Long id,
                                                 @RequestParam BookingStatus status) {
        return ApiResponse.ok(bookingService.updateStatus(id, status));
    }
}
