package com.ptit.tour.domain.tour.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.tour.dto.SaveTourDepartureRequest;
import com.ptit.tour.domain.tour.dto.SaveTourRequest;
import com.ptit.tour.domain.tour.dto.TourDetailDto;
import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.tour.enums.TourStatus;
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

import java.time.LocalDate;

@Tag(name = "Admin Tours")
@RestController
@RequestMapping("/admin/tours")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTourController {

    private final TourService tourService;

    @Operation(summary = "[Admin] Danh sách tour đầy đủ")
    @GetMapping
    public ApiResponse<PageResponse<TourSummaryDto>> searchAdmin(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long destinationId,
        @RequestParam(required = false) TourStatus status,
        @RequestParam(required = false) LocalDate departureDate,
        @RequestParam(required = false) Integer availableSlots,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(
            tourService.searchAdmin(keyword, destinationId, status, departureDate, availableSlots, pageable)));
    }

    @Operation(summary = "[Admin] Chi tiết tour theo ID")
    @GetMapping("/{id}")
    public ApiResponse<TourDetailDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(tourService.getAdminById(id));
    }

    @Operation(summary = "[Admin] Tạo tour mới")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TourDetailDto> create(@Valid @RequestBody SaveTourRequest request) {
        return ApiResponse.created(tourService.create(request));
    }

    @Operation(summary = "[Admin] Cập nhật tour")
    @PutMapping("/{id}")
    public ApiResponse<TourDetailDto> update(@PathVariable Long id,
                                             @Valid @RequestBody SaveTourRequest request) {
        return ApiResponse.ok(tourService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá hoặc lưu trữ tour")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tourService.delete(id);
        return ApiResponse.noContent("Đã xoá hoặc lưu trữ tour");
    }

    @Operation(summary = "[Admin] Tạo lịch khởi hành cho tour")
    @PostMapping("/{tourId}/departures")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TourDetailDto> createDeparture(@PathVariable Long tourId,
                                                      @Valid @RequestBody SaveTourDepartureRequest request) {
        return ApiResponse.created(tourService.createDeparture(tourId, request));
    }

    @Operation(summary = "[Admin] Cập nhật lịch khởi hành")
    @PutMapping("/{tourId}/departures/{departureId}")
    public ApiResponse<TourDetailDto> updateDeparture(@PathVariable Long tourId,
                                                      @PathVariable Long departureId,
                                                      @Valid @RequestBody SaveTourDepartureRequest request) {
        return ApiResponse.ok(tourService.updateDeparture(tourId, departureId, request));
    }

    @Operation(summary = "[Admin] Xoá hoặc huỷ lịch khởi hành")
    @DeleteMapping("/{tourId}/departures/{departureId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteDeparture(@PathVariable Long tourId, @PathVariable Long departureId) {
        tourService.deleteDeparture(tourId, departureId);
        return ApiResponse.noContent("Đã xoá hoặc huỷ lịch khởi hành");
    }
}
