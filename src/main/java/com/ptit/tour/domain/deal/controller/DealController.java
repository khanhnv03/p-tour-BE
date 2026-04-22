package com.ptit.tour.domain.deal.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.deal.dto.ApplyDealResponse;
import com.ptit.tour.domain.deal.dto.DealDto;
import com.ptit.tour.domain.deal.dto.SaveDealRequest;
import com.ptit.tour.domain.deal.service.DealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Deals")
@RestController
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @Operation(summary = "Deals đang hoạt động (public)")
    @GetMapping("/deals/public")
    public ApiResponse<List<DealDto>> getActive() {
        return ApiResponse.ok(dealService.getActivePublicDeals());
    }

    @Operation(summary = "Áp dụng mã promo")
    @GetMapping("/deals/apply")
    public ApiResponse<ApplyDealResponse> apply(@RequestParam String promoCode,
                                                  @RequestParam BigDecimal subtotal) {
        return ApiResponse.ok(dealService.applyPromoCode(promoCode, subtotal));
    }

    @Operation(summary = "Tìm deal auto-apply tốt nhất")
    @GetMapping("/deals/auto-apply")
    public ApiResponse<ApplyDealResponse> autoApply(@RequestParam BigDecimal subtotal) {
        return ApiResponse.ok(dealService.findBestAutoApply(subtotal));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Danh sách tất cả deals")
    @GetMapping("/admin/deals")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<DealDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(dealService.findAll(pageable)));
    }

    @Operation(summary = "[Admin] Chi tiết deal")
    @GetMapping("/admin/deals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DealDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(dealService.getById(id));
    }

    @Operation(summary = "[Admin] Tạo deal")
    @PostMapping("/admin/deals")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DealDto> create(@Valid @RequestBody SaveDealRequest request) {
        return ApiResponse.created(dealService.create(request));
    }

    @Operation(summary = "[Admin] Cập nhật deal")
    @PutMapping("/admin/deals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DealDto> update(@PathVariable Long id,
                                        @Valid @RequestBody SaveDealRequest request) {
        return ApiResponse.ok(dealService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá deal")
    @DeleteMapping("/admin/deals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dealService.delete(id);
        return ApiResponse.noContent("Đã xoá deal");
    }
}
