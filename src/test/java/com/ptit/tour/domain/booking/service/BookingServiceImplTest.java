package com.ptit.tour.domain.booking.service;

import com.ptit.tour.common.exception.BusinessException;
import com.ptit.tour.domain.booking.dto.CreateBookingRequest;
import com.ptit.tour.domain.booking.repository.BookingRepository;
import com.ptit.tour.domain.deal.entity.Deal;
import com.ptit.tour.domain.deal.enums.DealStatus;
import com.ptit.tour.domain.deal.enums.DisplayMode;
import com.ptit.tour.domain.deal.enums.DiscountType;
import com.ptit.tour.domain.deal.repository.DealRepository;
import com.ptit.tour.domain.tour.entity.Tour;
import com.ptit.tour.domain.tour.entity.TourDeparture;
import com.ptit.tour.domain.tour.enums.DepartureStatus;
import com.ptit.tour.domain.tour.enums.TourDifficulty;
import com.ptit.tour.domain.tour.enums.TourStatus;
import com.ptit.tour.domain.tour.repository.TourDepartureRepository;
import com.ptit.tour.domain.tour.repository.TourRepository;
import com.ptit.tour.domain.user.entity.User;
import com.ptit.tour.domain.user.enums.UserRole;
import com.ptit.tour.domain.user.enums.UserStatus;
import com.ptit.tour.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourDepartureRepository departureRepository;

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createShouldCalculateSubtotalTaxDiscountAndTotalInVnd() {
        User user = buildUser(7L);
        Tour tour = buildTour(3L, 10);
        TourDeparture departure = buildDeparture(11L, tour, 12);
        Deal deal = buildDeal(DisplayMode.COPY_CODE);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));
        when(departureRepository.findById(11L)).thenReturn(Optional.of(departure));
        when(dealRepository.findByPromoCode("AUTO2026")).thenReturn(Optional.of(deal));
        when(bookingRepository.findByBookingCode(anyString())).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = bookingService.create(7L,
            new CreateBookingRequest(3L, 11L, null, 2, "Alex PTIT", "alex@ptittour.com", "0900000000", "AUTO2026", null));

        assertThat(dto.subtotal()).isEqualByComparingTo("6980000.00");
        assertThat(dto.taxAmount()).isEqualByComparingTo("349000.00");
        assertThat(dto.discountAmount()).isEqualByComparingTo("300000.00");
        assertThat(dto.totalAmount()).isEqualByComparingTo("7029000.00");
    }

    @Test
    void createShouldRejectGuestCountAboveTourMaxGuests() {
        User user = buildUser(7L);
        Tour tour = buildTour(3L, 4);
        TourDeparture departure = buildDeparture(11L, tour, 12);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));
        when(departureRepository.findById(11L)).thenReturn(Optional.of(departure));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            bookingService.create(7L, new CreateBookingRequest(3L, 11L, null, 5, null, null, null, null, "Ghi chú")));

        assertThat(exception.getMessage()).contains("giới hạn tối đa");
    }

    @Test
    void createShouldRejectPromoCodeForAutoApplyDeal() {
        User user = buildUser(7L);
        Tour tour = buildTour(3L, 10);
        TourDeparture departure = buildDeparture(11L, tour, 12);
        Deal deal = buildDeal(DisplayMode.AUTO_APPLY);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));
        when(departureRepository.findById(11L)).thenReturn(Optional.of(departure));
        when(dealRepository.findByPromoCode("AUTO2026")).thenReturn(Optional.of(deal));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            bookingService.create(7L, new CreateBookingRequest(3L, 11L, null, 2, null, null, null, "AUTO2026", null)));

        assertThat(exception.getMessage()).contains("không hỗ trợ nhập mã promo");
    }

    @Test
    void createShouldRejectDealIdForCopyCodeDeal() {
        User user = buildUser(7L);
        Tour tour = buildTour(3L, 10);
        TourDeparture departure = buildDeparture(11L, tour, 12);
        Deal deal = buildDeal(DisplayMode.COPY_CODE);
        deal.setId(21L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(3L)).thenReturn(Optional.of(tour));
        when(departureRepository.findById(11L)).thenReturn(Optional.of(departure));
        when(dealRepository.findById(21L)).thenReturn(Optional.of(deal));

        BusinessException exception = assertThrows(BusinessException.class, () ->
            bookingService.create(7L, new CreateBookingRequest(3L, 11L, 21L, 2, null, null, null, null, null)));

        assertThat(exception.getMessage()).contains("yêu cầu nhập mã promo");
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

    private Tour buildTour(Long id, int maxGuests) {
        Tour tour = Tour.builder()
            .title("Bình minh trên đỉnh Langbiang")
            .slug("binh-minh-tren-dinh-langbiang")
            .durationDays(3)
            .durationNights(2)
            .maxGuests(maxGuests)
            .difficulty(TourDifficulty.MEDIUM)
            .pricePerPerson(BigDecimal.valueOf(3_490_000L))
            .status(TourStatus.PUBLISHED)
            .build();
        tour.setId(id);
        return tour;
    }

    private TourDeparture buildDeparture(Long id, Tour tour, int availableSlots) {
        TourDeparture departure = TourDeparture.builder()
            .tour(tour)
            .departureDate(LocalDate.of(2026, 5, 20))
            .availableSlots(availableSlots)
            .bookedSlots(0)
            .status(DepartureStatus.OPEN)
            .build();
        departure.setId(id);
        return departure;
    }

    private Deal buildDeal(DisplayMode displayMode) {
        return Deal.builder()
            .title("Ưu đãi hè 2026")
            .discountType(DiscountType.FIXED)
            .discountValue(BigDecimal.valueOf(300_000L))
            .promoCode("AUTO2026")
            .displayMode(displayMode)
            .minOrderValue(BigDecimal.ZERO)
            .validFrom(LocalDate.of(2026, 1, 1))
            .validTo(LocalDate.of(2026, 12, 31))
            .status(DealStatus.ACTIVE)
            .build();
    }
}
