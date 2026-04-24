package com.ptit.tour.domain.tour.dto;

import com.ptit.tour.domain.tour.enums.DepartureStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveTourDepartureRequest(
    @NotNull LocalDate departureDate,
    @Min(1) int availableSlots,
    @Min(0) int bookedSlots,
    BigDecimal priceOverride,
    @NotNull DepartureStatus status
) {}
