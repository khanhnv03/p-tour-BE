package com.ptit.tour.domain.destination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveDestinationRequest(
    @NotBlank @Size(max = 255) String name,
    String description,
    @Size(max = 500) String coverImageUrl,
    @Size(max = 100) String country,
    @Size(max = 100) String region,
    boolean featured
) {}
