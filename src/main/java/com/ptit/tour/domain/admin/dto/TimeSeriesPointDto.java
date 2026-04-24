package com.ptit.tour.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeSeriesPointDto(
    LocalDate date,
    BigDecimal value
) {}
