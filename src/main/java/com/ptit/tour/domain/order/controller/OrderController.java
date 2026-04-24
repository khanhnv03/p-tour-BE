package com.ptit.tour.domain.order.controller;

import com.ptit.tour.common.exception.BusinessException;
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
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
    private final Environment environment;

    @Operation(summary = "Tạo đơn hàng thanh toán")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.created(orderService.create(principal.getId(), request));
    }

    @Operation(summary = "Xem chi tiết đơn hàng của mình")
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderDto> getById(@PathVariable Long id,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(orderService.getById(id, principal.getId()));
    }

    @Operation(summary = "Đơn hàng theo booking")
    @GetMapping("/orders/booking/{bookingId}")
    public ApiResponse<OrderDto> getByBooking(@PathVariable Long bookingId,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(orderService.getByBookingId(bookingId, principal.getId()));
    }

    @Operation(summary = "Xác nhận thanh toán (webhook payment gateway)")
    @PostMapping("/orders/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDto> confirm(@PathVariable Long id,
                                          @RequestParam String transactionRef,
                                          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.ok(orderService.confirmPayment(id, transactionRef, idempotencyKey));
    }

    @Operation(summary = "[Dev] Mock thanh toán thành công — chỉ dùng trong môi trường phát triển")
    @PostMapping("/orders/{id}/mock-pay")
    public ApiResponse<OrderDto> mockPay(@PathVariable Long id,
                                          @AuthenticationPrincipal UserPrincipal principal,
                                          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (!environment.acceptsProfiles(Profiles.of("dev"))) {
            throw new BusinessException("Mock payment chỉ khả dụng trong môi trường dev", HttpStatus.NOT_FOUND);
        }
        // Verify ownership before mock payment
        orderService.getById(id, principal.getId());
        String mockRef = idempotencyKey == null || idempotencyKey.isBlank()
            ? "MOCK-" + System.currentTimeMillis()
            : "MOCK-" + idempotencyKey.trim();
        return ApiResponse.ok(orderService.confirmPayment(id, mockRef, idempotencyKey));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Chi tiết đơn hàng")
    @GetMapping("/admin/orders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDto> adminGetById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getByIdAdmin(id));
    }

    @Operation(summary = "[Admin] Danh sách đơn hàng")
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<OrderDto>> listAll(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) PaymentStatus status,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(orderService.findAll(userId, status, pageable)));
    }

    @Operation(summary = "[Admin] Hoàn tiền đơn hàng")
    @PostMapping("/admin/orders/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDto> refund(@PathVariable Long id) {
        return ApiResponse.ok(orderService.refund(id));
    }
}
