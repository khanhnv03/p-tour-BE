package com.ptit.tour.domain.wishlist.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.common.exception.ResourceNotFoundException;
import com.ptit.tour.domain.tour.service.TourService;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.domain.wishlist.dto.WishlistItemDto;
import com.ptit.tour.domain.wishlist.entity.Wishlist;
import com.ptit.tour.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    @Override
    @Transactional
    public WishlistItemDto add(Long userId, Long tourId) {
        if (wishlistRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new BusinessException("Tour đã có trong danh sách yêu thích", HttpStatus.CONFLICT);
        }
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var tour = tourService.getEntityById(tourId);

        Wishlist wishlist = Wishlist.builder().user(user).tour(tour).build();
        return WishlistItemDto.from(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public void remove(Long userId, Long tourId) {
        if (!wishlistRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new ResourceNotFoundException("Wishlist", "tourId", tourId);
        }
        wishlistRepository.deleteByUserIdAndTourId(userId, tourId);
    }

    @Override
    public boolean isWishlisted(Long userId, Long tourId) {
        return wishlistRepository.existsByUserIdAndTourId(userId, tourId);
    }

    @Override
    public Page<WishlistItemDto> getMyWishlist(Long userId, Pageable pageable) {
        return wishlistRepository.findByUserId(userId, pageable).map(WishlistItemDto::from);
    }
}
