package com.ptit.tour.domain.deal.dto;

import java.math.BigDecimal;

public record ApplyDealResponse(
    Long dealId,
    String promoCode,
    BigDecimal discountAmount,
    String message
) {}
