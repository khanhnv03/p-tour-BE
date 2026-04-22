package com.ptit.tour.domain.review.repository;

import com.ptit.tour.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByTourId(Long tourId, Pageable pageable);

    boolean existsByBookingId(Long bookingId);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tour.id = :tourId")
    Optional<Double> calcAverageRating(@Param("tourId") Long tourId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tour.id = :tourId")
    long countByTourId(@Param("tourId") Long tourId);
}
