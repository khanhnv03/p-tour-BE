package com.ptit.tour.domain.wishlist.service;

import com.ptit.tour.domain.wishlist.dto.WishlistItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistItemDto add(Long userId, Long tourId);
    void remove(Long userId, Long tourId);
    boolean isWishlisted(Long userId, Long tourId);
    Page<WishlistItemDto> getMyWishlist(Long userId, Pageable pageable);
}
