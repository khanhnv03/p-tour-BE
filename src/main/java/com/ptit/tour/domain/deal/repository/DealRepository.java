package com.ptit.tour.domain.deal.repository;

import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long> {

    Optional<Deal> findByPromoCode(String promoCode);

    Page<Deal> findByStatus(DealStatus status, Pageable pageable);

    @Query("""
        SELECT d FROM Deal d
        WHERE (:status IS NULL OR d.status = :status)
          AND (:keyword IS NULL
            OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.promoCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (
            :dateState IS NULL
            OR (:dateState = 'UPCOMING' AND d.validFrom > :today)
            OR (:dateState = 'ACTIVE_NOW' AND d.validFrom <= :today AND d.validTo >= :today)
            OR (:dateState = 'EXPIRED' AND d.validTo < :today)
          )
        """)
    Page<Deal> searchAdmin(@Param("status") DealStatus status,
                           @Param("keyword") String keyword,
                           @Param("dateState") String dateState,
                           @Param("today") LocalDate today,
                           Pageable pageable);

    /** Find active auto-apply deals valid today with min order satisfied. */
    @Query("""
        SELECT d FROM Deal d
        WHERE d.status = 'ACTIVE'
          AND d.displayMode = 'AUTO_APPLY'
          AND d.validFrom <= :today
          AND d.validTo >= :today
          AND d.minOrderValue <= :subtotal
          AND (d.usageLimit IS NULL OR d.usageCount < d.usageLimit)
        ORDER BY d.discountValue DESC
        """)
    List<Deal> findBestAutoApply(@Param("today") LocalDate today,
                                  @Param("subtotal") BigDecimal subtotal);
}
