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

@Tag(name = "Admin Destinations")
@RestController
@RequestMapping("/admin/destinations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDestinationController {

    private final DestinationService destinationService;

    @Operation(summary = "[Admin] Danh sách điểm đến")
    @GetMapping
    public ApiResponse<PageResponse<DestinationDto>> list(
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(destinationService.findAll(keyword, pageable)));
    }

    @Operation(summary = "[Admin] Chi tiết điểm đến")
    @GetMapping("/{id}")
    public ApiResponse<DestinationDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.getById(id));
    }

    @Operation(summary = "[Admin] Tạo điểm đến")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DestinationDto> create(@Valid @RequestBody SaveDestinationRequest request) {
        return ApiResponse.created(destinationService.create(request));
    }

    @Operation(summary = "[Admin] Cập nhật điểm đến")
    @PutMapping("/{id}")
    public ApiResponse<DestinationDto> update(@PathVariable Long id,
                                              @Valid @RequestBody SaveDestinationRequest request) {
        return ApiResponse.ok(destinationService.update(id, request));
    }

    @Operation(summary = "[Admin] Xoá điểm đến")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.noContent("Đã xoá điểm đến");
    }
}
