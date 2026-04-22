package com.ptit.tour.domain.review.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.service.BookingService;
import com.ptit.tour.domain.review.dto.CreateReviewRequest;
import com.ptit.tour.domain.review.dto.ReviewDto;
import com.ptit.tour.domain.review.entity.Review;
import com.ptit.tour.domain.review.repository.ReviewRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewDto create(Long userId, CreateReviewRequest req) {
        Booking booking = bookingService.getEntityById(req.bookingId());

        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException("Không có quyền đánh giá booking này");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("Chỉ được đánh giá sau khi hoàn thành tour");
        }
        if (reviewRepository.existsByBookingId(req.bookingId())) {
            throw new BusinessException("Booking này đã được đánh giá");
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Review review = Review.builder()
            .tour(booking.getTour()).user(user).booking(booking)
            .rating(req.rating()).comment(req.comment()).verified(true)
            .build();
        review = reviewRepository.save(review);

        updateTourRating(booking.getTour());
        return ReviewDto.from(review);
    }

    @Override
    public Page<ReviewDto> getByTour(Long tourId, Pageable pageable) {
        return reviewRepository.findByTourId(tourId, pageable).map(ReviewDto::from);
    }

    @Override
    public ReviewDto getById(Long id) {
        return ReviewDto.from(reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review", id)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        reviewRepository.delete(review);
        updateTourRating(review.getTour());
    }

    private void updateTourRating(Tour tour) {
        double avg = reviewRepository.calcAverageRating(tour.getId()).orElse(0.0);
        long count = reviewRepository.countByTourId(tour.getId());
        tour.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        tour.setReviewCount((int) count);
    }
}
