package com.ptit.tour.domain.booking.repository;

import com.ptit.tour.domain.booking.entity.Booking;
import com.ptit.tour.domain.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    List<Booking> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    long countByStatus(BookingStatus status);

    long countByUserId(Long userId);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant from, Instant to);

    boolean existsByUserIdAndTourIdAndStatusIn(Long userId, Long tourId, List<BookingStatus> statuses);

    boolean existsByTourId(Long tourId);

    @Query("""
        SELECT b FROM Booking b
        WHERE (:userId IS NULL OR b.user.id = :userId)
          AND (:status IS NULL OR b.status = :status)
        ORDER BY b.createdAt DESC
        """)
    Page<Booking> findAllFiltered(@Param("userId") Long userId,
                                   @Param("status") BookingStatus status,
                                   Pageable pageable);

    @Query("""
        SELECT FUNCTION('DATE', b.createdAt), COUNT(b)
        FROM Booking b
        WHERE b.createdAt >= :from
          AND b.createdAt < :to
        GROUP BY FUNCTION('DATE', b.createdAt)
        ORDER BY FUNCTION('DATE', b.createdAt)
        """)
    List<Object[]> bookingsByDay(@Param("from") Instant from, @Param("to") Instant to);
}
