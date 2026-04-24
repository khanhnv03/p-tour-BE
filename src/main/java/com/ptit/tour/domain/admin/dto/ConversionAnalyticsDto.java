package com.ptit.tour.domain.admin.dto;

public record ConversionAnalyticsDto(
    long totalBookings,
    long paidOrders,
    long pendingOrders,
    long refundedOrders,
    double paymentConversionRate
) {}
