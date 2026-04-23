package com.ptit.tour.domain.review.service;

import com.ptit.tour.domain.review.dto.CreateReviewRequest;
import com.ptit.tour.domain.review.dto.ReviewDto;
import com.ptit.tour.domain.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto create(Long userId, CreateReviewRequest request);

    Page<ReviewDto> getByTour(Long tourId, Pageable pageable);

    ReviewDto getById(Long id);

    ReviewDto updateStatus(Long id, ReviewStatus status);

    Page<ReviewDto> getAll(Pageable pageable);

    void delete(Long id);
}
