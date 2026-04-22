package com.ptit.tour.domain.order.dto;

import com.ptit.tour.domain.order.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequest(
    @NotNull Long bookingId,
    @NotNull PaymentMethod paymentMethod,
    @Pattern(regexp = "\\d{4}") String cardLastFour
) {}
