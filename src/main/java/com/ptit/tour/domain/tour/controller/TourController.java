package com.ptit.tour.domain.tour.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.service.TourService;
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
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Tours")
@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @Operation(summary = "Tìm kiếm & lọc tour (public)")
    @GetMapping
    public ApiResponse<PageResponse<TourSummaryDto>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long destinationId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false) Integer guests,
        @RequestParam(required = false) Integer duration,
        @RequestParam(required = false) TourDifficulty difficulty,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) BigDecimal minRating,
        @PageableDefault(size = 12) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(
            tourService.search(keyword, destinationId, date, guests, duration, difficulty, minPrice, maxPrice, minRating, pageable)));
    }

    @Operation(summary = "Tour nổi bật (top rating)")
    @GetMapping("/featured")
    public ApiResponse<List<TourSummaryDto>> getFeatured(
        @RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.ok(tourService.getFeatured(limit));
    }

    @Operation(summary = "Tour phổ biến (top booking)")
    @GetMapping("/popular")
    public ApiResponse<List<TourSummaryDto>> getPopular(
        @RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.ok(tourService.getPopular(limit));
    }

    @Operation(summary = "Chi tiết tour theo ID")
    @GetMapping("/{id}")
    public ApiResponse<TourDetailDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(tourService.getById(id));
    }

    @Operation(summary = "Chi tiết tour theo slug")
    @GetMapping("/slug/{slug}")
    public ApiResponse<TourDetailDto> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(tourService.getBySlug(slug));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Tạo tour mới")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourDetailDto> create(@Valid @RequestBody SaveTourRequest request) {
        return ApiResponse.created(tourService.create(request));
    }

    @Operation(summary = "[Admin] Cập nhật tour")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TourDetailDto> update(@PathVariable Long id,
                                              @Valid @RequestBody SaveTourRequest request) {
        return ApiResponse.ok(tourService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá tour")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tourService.delete(id);
        return ApiResponse.noContent("Đã xoá tour");
    }
}
