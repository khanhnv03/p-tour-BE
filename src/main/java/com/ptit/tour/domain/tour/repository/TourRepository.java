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
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {

    Optional<Tour> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Tour> findByStatus(TourStatus status, Pageable pageable);

    Page<Tour> findByDestinationIdAndStatus(Long destinationId, TourStatus status, Pageable pageable);

    @Query("""
        SELECT t FROM Tour t
        WHERE t.status = 'PUBLISHED'
          AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:destinationId IS NULL OR t.destination.id = :destinationId)
          AND (:difficulty IS NULL OR t.difficulty = :difficulty)
          AND (:minPrice IS NULL OR t.pricePerPerson >= :minPrice)
          AND (:maxPrice IS NULL OR t.pricePerPerson <= :maxPrice)
        """)
    Page<Tour> search(
        @Param("keyword") String keyword,
        @Param("destinationId") Long destinationId,
        @Param("difficulty") TourDifficulty difficulty,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}
