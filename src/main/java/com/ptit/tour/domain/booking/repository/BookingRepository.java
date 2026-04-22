package com.ptit.tour.domain.booking.repository;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    boolean existsByUserIdAndTourIdAndStatusIn(Long userId, Long tourId, java.util.List<BookingStatus> statuses);

    @Query("""
        SELECT b FROM Booking b
        WHERE (:userId IS NULL OR b.user.id = :userId)
          AND (:status IS NULL OR b.status = :status)
        ORDER BY b.createdAt DESC
        """)
    Page<Booking> findAllFiltered(@Param("userId") Long userId,
                                   @Param("status") BookingStatus status,
                                   Pageable pageable);
}
