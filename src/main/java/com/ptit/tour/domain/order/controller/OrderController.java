package com.ptit.tour.domain.order.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.order.dto.CreateOrderRequest;
import com.ptit.tour.domain.order.dto.OrderDto;
import com.ptit.tour.domain.order.enums.PaymentStatus;
import com.ptit.tour.domain.order.service.OrderService;
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

@Tag(name = "Orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Tạo đơn hàng thanh toán")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.created(orderService.create(principal.getId(), request));
    }

    @Operation(summary = "Xem chi tiết đơn hàng")
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @Operation(summary = "Đơn hàng theo booking")
    @GetMapping("/orders/booking/{bookingId}")
    public ApiResponse<OrderDto> getByBooking(@PathVariable Long bookingId) {
        return ApiResponse.ok(orderService.getByBookingId(bookingId));
    }

    @Operation(summary = "Xác nhận thanh toán (webhook / callback)")
    @PostMapping("/orders/{id}/confirm")
    public ApiResponse<OrderDto> confirm(@PathVariable Long id,
                                          @RequestParam String transactionRef) {
        return ApiResponse.ok(orderService.confirmPayment(id, transactionRef));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Danh sách đơn hàng")
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<OrderDto>> listAll(
        @RequestParam(required = false) PaymentStatus status,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(orderService.findAll(status, pageable)));
    }

    @Operation(summary = "[Admin] Hoàn tiền đơn hàng")
    @PostMapping("/admin/orders/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDto> refund(@PathVariable Long id) {
        return ApiResponse.ok(orderService.refund(id));
    }
}
