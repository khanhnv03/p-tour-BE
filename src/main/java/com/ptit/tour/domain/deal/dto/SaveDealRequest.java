package com.ptit.tour.domain.deal.dto;

import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.enums.DiscountType;
import com.ptit.tour.domain.deal.enums.DisplayMode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveDealRequest(
    @NotBlank @Size(max = 500) String title,
    String description,
    @Size(max = 500) String campaignImageUrl,
    @Size(max = 100) String badgeText,
    @Size(max = 100) String category,
    @NotNull DiscountType discountType,
    @NotNull @DecimalMin("0") BigDecimal discountValue,
    @Size(max = 50) String promoCode,
    @NotNull DisplayMode displayMode,
    @DecimalMin("0") BigDecimal minOrderValue,
    @DecimalMin("0") BigDecimal maxDiscountAmount,
    @Min(1) Integer usageLimit,
    @NotNull LocalDate validFrom,
    @NotNull LocalDate validTo,
    @NotNull DealStatus status
) {}
