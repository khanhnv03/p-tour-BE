package com.ptit.tour.domain.tour.repository;

import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {

    Optional<Tour> findBySlug(String slug);
    Optional<Tour> findBySlugAndStatus(String slug, TourStatus status);
    Optional<Tour> findByIdAndStatus(Long id, TourStatus status);

    boolean existsBySlug(String slug);

    Page<Tour> findByStatus(TourStatus status, Pageable pageable);

    Page<Tour> findByDestinationIdAndStatus(Long destinationId, TourStatus status, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT t FROM Tour t
        LEFT JOIN t.departures d
        WHERE t.status = 'PUBLISHED'
          AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:destinationId IS NULL OR t.destination.id = :destinationId)
          AND (:durationDays IS NULL OR t.durationDays = :durationDays)
          AND (:difficulty IS NULL OR t.difficulty = :difficulty)
          AND (:minPrice IS NULL OR t.pricePerPerson >= :minPrice)
          AND (:maxPrice IS NULL OR t.pricePerPerson <= :maxPrice)
          AND (:minRating IS NULL OR t.rating >= :minRating)
          AND (
            (:departureDate IS NULL AND :guestCount IS NULL)
            OR (
              d.status = 'OPEN'
              AND (:departureDate IS NULL OR d.departureDate = :departureDate)
              AND (:guestCount IS NULL OR (d.availableSlots - d.bookedSlots) >= :guestCount)
            )
          )
        """,
        countQuery = """
        SELECT COUNT(DISTINCT t) FROM Tour t
        LEFT JOIN t.departures d
        WHERE t.status = 'PUBLISHED'
          AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:destinationId IS NULL OR t.destination.id = :destinationId)
          AND (:durationDays IS NULL OR t.durationDays = :durationDays)
          AND (:difficulty IS NULL OR t.difficulty = :difficulty)
          AND (:minPrice IS NULL OR t.pricePerPerson >= :minPrice)
          AND (:maxPrice IS NULL OR t.pricePerPerson <= :maxPrice)
          AND (:minRating IS NULL OR t.rating >= :minRating)
          AND (
            (:departureDate IS NULL AND :guestCount IS NULL)
            OR (
              d.status = 'OPEN'
              AND (:departureDate IS NULL OR d.departureDate = :departureDate)
              AND (:guestCount IS NULL OR (d.availableSlots - d.bookedSlots) >= :guestCount)
            )
          )
        """)
    Page<Tour> search(
        @Param("keyword") String keyword,
        @Param("destinationId") Long destinationId,
        @Param("departureDate") LocalDate departureDate,
        @Param("guestCount") Integer guestCount,
        @Param("durationDays") Integer durationDays,
        @Param("difficulty") TourDifficulty difficulty,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minRating") BigDecimal minRating,
        Pageable pageable
    );

    @Query("SELECT t FROM Tour t WHERE t.status = 'PUBLISHED' ORDER BY t.rating DESC, t.reviewCount DESC")
    List<Tour> findFeatured(Pageable pageable);

    @Query("SELECT t FROM Tour t WHERE t.status = 'PUBLISHED' ORDER BY t.bookingCount DESC, t.rating DESC")
    List<Tour> findPopular(Pageable pageable);
}
