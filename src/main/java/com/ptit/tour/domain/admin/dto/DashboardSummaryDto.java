package com.ptit.tour.domain.admin.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
    BigDecimal revenue,
    long customerCount,
    long activeTours,
    BigDecimal averageRating,
    double revenueGrowthPercent,
    double customerGrowthPercent,
    long pendingBookings,
    long newContacts,
    long newsletterSubscribers
) {}
