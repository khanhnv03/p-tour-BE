package com.ptit.tour.domain.wishlist.repository;

import com.ptit.tour.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);
    Optional<Wishlist> findByUserIdAndTourId(Long userId, Long tourId);
    boolean existsByUserIdAndTourId(Long userId, Long tourId);
    void deleteByUserIdAndTourId(Long userId, Long tourId);
}
