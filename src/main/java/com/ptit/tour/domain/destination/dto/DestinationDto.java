package com.ptit.tour.domain.destination.dto;

import com.ptit.tour.domain.destination.entity.Destination;

public record DestinationDto(
    Long id,
    String name,
    String slug,
    String description,
    String coverImageUrl,
    String country,
    String region,
    boolean featured,
    int tourCount
) {
    public static DestinationDto from(Destination d) {
        return new DestinationDto(d.getId(), d.getName(), d.getSlug(), d.getDescription(),
            d.getCoverImageUrl(), d.getCountry(), d.getRegion(), d.isFeatured(), d.getTourCount());
    }
}
