package com.ptit.tour.domain.tour.dto;

import com.ptit.tour.domain.tour.enums.InclusionType;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record SaveTourRequest(
    @NotNull Long destinationId,
    @NotBlank @Size(max = 500) String title,
    String description,
    @Min(1) int durationDays,
    @Min(0) int durationNights,
    @Min(1) int maxGuests,
    @NotNull TourDifficulty difficulty,
    @NotNull @DecimalMin("0.0") BigDecimal pricePerPerson,
    @Size(max = 500) String coverImageUrl,
    @NotNull TourStatus status,
    @Valid List<GalleryImageRequest> galleryImages,
    @Valid List<HighlightRequest> highlights,
    @Valid List<InclusionRequest> inclusions,
    @Valid List<ItineraryDayRequest> itineraryDays
) {
    public record GalleryImageRequest(
        @NotBlank String imageUrl,
        int sortOrder
    ) {}

    public record HighlightRequest(
        @NotBlank String icon,
        @NotBlank String label,
        int sortOrder
    ) {}

    public record InclusionRequest(
        @NotNull InclusionType type,
        @NotBlank String description,
        int sortOrder
    ) {}

    public record ItineraryDayRequest(
        @Min(1) int dayNumber,
        @NotBlank String title,
        String summary,
        String coverImageUrl,
        @Valid List<ActivityRequest> activities
    ) {}

    public record ActivityRequest(
        @NotNull LocalTime activityTime,
        String title,
        @NotBlank String description,
        int sortOrder
    ) {}
}
