package com.ptit.tour.domain.deal.dto;

import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.enums.DiscountType;
import com.ptit.tour.domain.deal.enums.DisplayMode;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DealDto(
    Long id,
    String title,
    String description,
    String campaignImageUrl,
    String badgeText,
    String category,
    DiscountType discountType,
    BigDecimal discountValue,
    String promoCode,
    DisplayMode displayMode,
    BigDecimal minOrderValue,
    BigDecimal maxDiscountAmount,
    Integer usageLimit,
    int usageCount,
    LocalDate validFrom,
    LocalDate validTo,
    DealStatus status
) {
    public static DealDto from(Deal d) {
        return from(d, d.getStatus());
    }

    public static DealDto from(Deal d, DealStatus effectiveStatus) {
        return new DealDto(d.getId(), d.getTitle(), d.getDescription(), d.getCampaignImageUrl(),
            d.getBadgeText(), d.getCategory(), d.getDiscountType(), d.getDiscountValue(),
            d.getPromoCode(), d.getDisplayMode(), d.getMinOrderValue(), d.getMaxDiscountAmount(),
            d.getUsageLimit(), d.getUsageCount(), d.getValidFrom(), d.getValidTo(), effectiveStatus);
    }
}
