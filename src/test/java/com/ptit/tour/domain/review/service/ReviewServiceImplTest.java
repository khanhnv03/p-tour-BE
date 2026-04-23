package com.ptit.tour.domain.review.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import com.ptit.tour.domain.booking.service.BookingService;
import com.ptit.tour.domain.review.dto.CreateReviewRequest;
import com.ptit.tour.domain.review.dto.ReviewDto;
import com.ptit.tour.domain.review.entity.Review;
import com.ptit.tour.domain.review.enums.ReviewStatus;
import com.ptit.tour.domain.review.repository.ReviewRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void createShouldStoreReviewAsPending() {
        User user = buildUser(7L, "alex@ptittour.com", "Alex PTIT");
        Booking booking = buildCompletedBooking(11L, user);

        when(bookingService.getEntityById(11L)).thenReturn(booking);
        when(reviewRepository.existsByBookingId(11L)).thenReturn(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            saved.setId(21L);
            saved.setCreatedAt(Instant.parse("2026-04-24T00:00:00Z"));
            return saved;
        });

        ReviewDto dto = reviewService.create(7L, new CreateReviewRequest(11L, 5, "Tour rất ổn"));

        assertThat(dto.reviewStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(dto.bookingId()).isEqualTo(11L);
        assertThat(dto.userFullName()).isEqualTo("Alex PTIT");
    }

    @Test
    void createShouldRejectReviewFromAnotherUser() {
        User bookingOwner = buildUser(7L, "alex@ptittour.com", "Alex PTIT");
        Booking booking = buildCompletedBooking(11L, bookingOwner);

        when(bookingService.getEntityById(11L)).thenReturn(booking);

        assertThrows(BusinessException.class, () ->
            reviewService.create(99L, new CreateReviewRequest(11L, 5, "Không hợp lệ")));
    }

    @Test
    void getByTourShouldOnlyRequestApprovedReviews() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(reviewRepository.findByTourIdAndReviewStatus(5L, ReviewStatus.APPROVED, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        reviewService.getByTour(5L, pageable);

        verify(reviewRepository).findByTourIdAndReviewStatus(5L, ReviewStatus.APPROVED, pageable);
    }

    private Booking buildCompletedBooking(Long bookingId, User user) {
        Tour tour = Tour.builder()
            .title("Bình minh trên đỉnh Langbiang")
            .slug("binh-minh-tren-dinh-langbiang")
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .build();
        tour.setId(3L);

        Booking booking = Booking.builder()
            .bookingCode("BK-1934")
            .user(user)
            .tour(tour)
            .status(BookingStatus.COMPLETED)
            .build();
        booking.setId(bookingId);
        return booking;
    }

    private User buildUser(Long id, String email, String fullName) {
        User user = User.builder()
            .email(email)
            .fullName(fullName)
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
        user.setId(id);
        return user;
    }
}
