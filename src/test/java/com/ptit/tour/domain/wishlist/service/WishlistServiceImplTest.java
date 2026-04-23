package com.ptit.tour.domain.wishlist.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.domain.destination.entity.Destination;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.service.TourService;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import com.ptit.tour.domain.wishlist.dto.WishlistItemDto;
import com.ptit.tour.domain.wishlist.entity.Wishlist;
import com.ptit.tour.domain.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourService tourService;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @Test
    void addShouldRejectDuplicateWishlistItem() {
        when(wishlistRepository.existsByUserIdAndTourId(7L, 3L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> wishlistService.add(7L, 3L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void addShouldReturnWishlistItemWhenCreated() {
        User user = buildUser(7L);
        Tour tour = buildTour(3L);

        when(wishlistRepository.existsByUserIdAndTourId(7L, 3L)).thenReturn(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourService.getEntityById(3L)).thenReturn(tour);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist wishlist = invocation.getArgument(0);
            wishlist.setId(15L);
            wishlist.setCreatedAt(Instant.parse("2026-04-24T00:00:00Z"));
            return wishlist;
        });

        WishlistItemDto dto = wishlistService.add(7L, 3L);

        assertThat(dto.wishlistId()).isEqualTo(15L);
        assertThat(dto.tour().slug()).isEqualTo("binh-minh-tren-dinh-langbiang");
        assertThat(dto.tour().destinationName()).isEqualTo("Đà Lạt");
    }

    private User buildUser(Long id) {
        User user = User.builder()
            .email("alex@ptittour.com")
            .fullName("Alex PTIT")
            .role(UserRole.CUSTOMER)
            .status(UserStatus.ACTIVE)
            .build();
        user.setId(id);
        return user;
    }

    private Tour buildTour(Long id) {
        Destination destination = Destination.builder()
            .name("Đà Lạt")
            .slug("da-lat")
            .country("Việt Nam")
            .build();
        destination.setId(1L);

        Tour tour = Tour.builder()
            .destination(destination)
            .title("Bình minh trên đỉnh Langbiang")
            .slug("binh-minh-tren-dinh-langbiang")
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .status(TourStatus.PUBLISHED)
            .build();
        tour.setId(id);
        return tour;
    }
}
