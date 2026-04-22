package com.ptit.tour.domain.destination.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.domain.destination.dto.DestinationDto;
import com.ptit.tour.domain.destination.dto.SaveDestinationRequest;
import com.ptit.tour.domain.destination.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Destinations")
@RestController
@RequestMapping("/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @Operation(summary = "Điểm đến nổi bật (trang chủ)")
    @GetMapping("/featured")
    public ApiResponse<List<DestinationDto>> getFeatured() {
        return ApiResponse.ok(destinationService.getFeatured());
    }

    @Operation(summary = "Danh sách tất cả điểm đến")
    @GetMapping
    public ApiResponse<PageResponse<DestinationDto>> list(
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(destinationService.findAll(keyword, pageable)));
    }

    @Operation(summary = "Chi tiết điểm đến theo ID")
    @GetMapping("/{id}")
    public ApiResponse<DestinationDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.getById(id));
    }

    @Operation(summary = "Chi tiết điểm đến theo slug")
    @GetMapping("/slug/{slug}")
    public ApiResponse<DestinationDto> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(destinationService.getBySlug(slug));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Operation(summary = "[Admin] Tạo điểm đến")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DestinationDto> create(@Valid @RequestBody SaveDestinationRequest request) {
        return ApiResponse.created(destinationService.create(request));
    }

    @Operation(summary = "[Admin] Cập nhật điểm đến")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DestinationDto> update(@PathVariable Long id,
                                               @Valid @RequestBody SaveDestinationRequest request) {
        return ApiResponse.ok(destinationService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá điểm đến")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.noContent("Đã xoá điểm đến");
    }
}
