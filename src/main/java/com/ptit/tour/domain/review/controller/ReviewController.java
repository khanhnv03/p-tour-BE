package com.ptit.tour.domain.review.controller;

import com.ptit.tour.common.response.ApiResponse;
import com.ptit.tour.common.response.PageResponse;
import com.ptit.tour.common.security.UserPrincipal;
import com.ptit.tour.domain.review.dto.CreateReviewRequest;
import com.ptit.tour.domain.review.dto.ReviewDto;
import com.ptit.tour.domain.review.service.ReviewService;
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

@Tag(name = "Reviews")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Đánh giá tour (sau khi hoàn thành)")
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.created(reviewService.create(principal.getId(), request));
    }

    @Operation(summary = "Danh sách reviews của một tour")
    @GetMapping("/tours/{tourId}/reviews")
    public ApiResponse<PageResponse<ReviewDto>> getByTour(
        @PathVariable Long tourId,
        @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.ok(PageResponse.of(reviewService.getByTour(tourId, pageable)));
    }

    @Operation(summary = "[Admin] Xoá review")
    @DeleteMapping("/admin/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ApiResponse.noContent("Đã xoá review");
    }
}
