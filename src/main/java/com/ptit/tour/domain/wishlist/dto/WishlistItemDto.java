package com.ptit.tour.domain.wishlist.dto;

import com.ptit.tour.domain.tour.dto.TourSummaryDto;
import com.ptit.tour.domain.wishlist.entity.Wishlist;

import java.time.Instant;

public record WishlistItemDto(
    Long wishlistId,
    TourSummaryDto tour,
    Instant savedAt
) {
    public static WishlistItemDto from(Wishlist w) {
        return new WishlistItemDto(w.getId(), TourSummaryDto.from(w.getTour()), w.getCreatedAt());
    }
}
