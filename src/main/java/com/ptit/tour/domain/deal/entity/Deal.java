package com.ptit.tour.domain.deal.entity;

import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.enums.DiscountType;
import com.ptit.tour.domain.deal.enums.DisplayMode;
import com.ptit.tour.domain.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deals", indexes = {
    @Index(name = "idx_deals_status", columnList = "status"),
    @Index(name = "idx_deals_dates", columnList = "valid_from, valid_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "campaign_image_url", length = 500)
    private String campaignImageUrl;

    @Column(name = "badge_text", length = 100)
    private String badgeText;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "promo_code", unique = true, length = 50)
    private String promoCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_mode", nullable = false, length = 20)
    private DisplayMode displayMode;

    @Column(name = "min_order_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DealStatus status = DealStatus.DRAFT;

    /** Compute actual discount amount for a given order subtotal. */
    public BigDecimal computeDiscount(BigDecimal subtotal) {
        if (discountType == DiscountType.FIXED) {
            return discountValue.min(subtotal);
        }
        BigDecimal calculated = subtotal.multiply(discountValue)
            .divide(BigDecimal.valueOf(100));
        return maxDiscountAmount != null ? calculated.min(maxDiscountAmount) : calculated;
    }
}
